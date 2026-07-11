package com.flowingsun.warresource.module.ore.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public final class OreDropConfigLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_ROOT = "warresource";
    private static final String ORE_DIR = "ore";
    private static final String DEFAULT_FILE = "defult";

    private OreDropConfigLoader() {
    }

    public static OreDropConfig load() {
        Path oreDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_ROOT).resolve(ORE_DIR);
        try {
            Files.createDirectories(oreDir);

            List<OreDropRule> defaultRules = loadDefaultRules(oreDir);
            Map<String, List<OreDropRule>> townRules = loadTownRules(oreDir);
            LOGGER.info("Loaded ore drop config: {} default rules, {} town files", defaultRules.size(), townRules.size());
            return new OreDropConfig(defaultRules, townRules);
        } catch (Exception e) {
            LOGGER.error("Failed to load ore drop config from {}. All ore drop chances are now zero.", oreDir, e);
            return OreDropConfig.empty();
        }
    }

    private static List<OreDropRule> loadDefaultRules(Path oreDir) throws IOException {
        Path defaultFile = oreDir.resolve(DEFAULT_FILE);
        if (!Files.exists(defaultFile)) {
            return List.of();
        }
        try {
            return parseFile(defaultFile, false).rules();
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load ore config file: {}", defaultFile, e);
            throw e;
        }
    }

    private static Map<String, List<OreDropRule>> loadTownRules(Path oreDir) throws IOException {
        Map<String, List<OreDropRule>> townRules = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(oreDir)) {
            List<Path> paths = files
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals(DEFAULT_FILE))
                    .sorted()
                    .toList();

            for (Path path : paths) {
                try {
                    ParsedOreFile parsed = parseFile(path, true);
                    townRules.put(parsed.townId(), parsed.rules());
                } catch (RuntimeException e) {
                    LOGGER.error("Failed to load ore config file: {}", path, e);
                    throw e;
                }
            }
        }
        return townRules;
    }

    private static ParsedOreFile parseFile(Path file, boolean requireTownId) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file", e);
        }

        String townId = "";
        String currentOreName = null;
        Double currentChance = null;
        List<OreDropRule> rules = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i)).trim();
            if (line.isEmpty()) {
                continue;
            }

            KeyValue keyValue = parseKeyValue(file, i + 1, line);
            switch (keyValue.key()) {
                case "townid" -> {
                    if (currentOreName != null || currentChance != null) {
                        throw parseError(file, i + 1, "townid must appear before ore rules");
                    }
                    townId = keyValue.value();
                }
                case "orename" -> {
                    if (currentOreName != null && currentChance == null) {
                        throw parseError(file, i + 1, "previous orename has no chance");
                    }
                    currentOreName = keyValue.value();
                    currentChance = null;
                }
                case "chance" -> {
                    if (currentOreName == null) {
                        throw parseError(file, i + 1, "chance appears before orename");
                    }
                    currentChance = parseChance(file, i + 1, keyValue.value());
                    rules.add(new OreDropRule(currentOreName, currentChance));
                    currentOreName = null;
                    currentChance = null;
                }
                default -> throw parseError(file, i + 1, "unknown key: " + keyValue.key());
            }
        }

        if (currentOreName != null || currentChance != null) {
            throw parseError(file, lines.size(), "unfinished ore rule");
        }
        if (requireTownId && townId.isBlank()) {
            throw parseError(file, 1, "town config file requires townid");
        }
        validateTotalChance(file, rules);

        return new ParsedOreFile(townId, rules);
    }

    private static KeyValue parseKeyValue(Path file, int lineNumber, String line) {
        int colon = line.indexOf(':');
        int fullWidthColon = line.indexOf('：');
        if (colon < 0 || (fullWidthColon >= 0 && fullWidthColon < colon)) {
            colon = fullWidthColon;
        }
        if (colon < 0) {
            throw parseError(file, lineNumber, "missing ':'");
        }

        String key = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
        String value = unquote(line.substring(colon + 1).trim());
        if (key.isBlank() || value.isBlank()) {
            throw parseError(file, lineNumber, "empty key or value");
        }
        return new KeyValue(key, value);
    }

    private static double parseChance(Path file, int lineNumber, String value) {
        try {
            double chance = Double.parseDouble(value);
            if (chance < 0.0D || chance > 1.0D) {
                throw parseError(file, lineNumber, "chance must be between 0 and 1");
            }
            return chance;
        } catch (NumberFormatException e) {
            throw parseError(file, lineNumber, "chance is not a number");
        }
    }

    private static void validateTotalChance(Path file, List<OreDropRule> rules) {
        double totalChance = rules.stream().mapToDouble(OreDropRule::chance).sum();
        if (totalChance > 1.0D) {
            throw parseError(file, 1, "total chance must not be greater than 1");
        }
    }

    private static String stripComment(String line) {
        int comment = line.indexOf('#');
        return comment >= 0 ? line.substring(0, comment) : line;
    }

    private static String unquote(String value) {
        String result = value.trim();
        if (result.length() >= 2) {
            char first = result.charAt(0);
            char last = result.charAt(result.length() - 1);
            if ((first == '"' && last == '"')
                    || (first == '\'' && last == '\'')
                    || (first == '“' && last == '”')
                    || (first == '‘' && last == '’')) {
                return result.substring(1, result.length() - 1).trim();
            }
        }
        return result;
    }

    private static IllegalArgumentException parseError(Path file, int lineNumber, String message) {
        return new IllegalArgumentException(file + ":" + lineNumber + " " + message);
    }

    private record ParsedOreFile(String townId, List<OreDropRule> rules) {
    }

    private record KeyValue(String key, String value) {
    }
}
