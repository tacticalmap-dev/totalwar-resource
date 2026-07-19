package com.flowingsun.warresource.module.ore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class OreDropConfigLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_ROOT = "warresource";
    private static final String ORE_DIR = "ore";
    private static final String DEFAULT_FILE = "defult.json";

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
            writeDefaultFile(defaultFile);
            return List.of();
        }

        try {
            OreConfigFile config = readConfigFile(defaultFile);
            List<OreDropRule> rules = toRules(defaultFile, config.ores);
            validateTotalChance(defaultFile, rules);
            return rules;
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
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals(DEFAULT_FILE))
                    .sorted()
                    .toList();

            for (Path path : paths) {
                try {
                    OreConfigFile config = readConfigFile(path);
                    if (config.townid == null || config.townid.isBlank()) {
                        throw parseError(path, "town config file requires townid");
                    }
                    List<OreDropRule> rules = toRules(path, config.ores);
                    validateTotalChance(path, rules);
                    townRules.put(config.townid.trim(), rules);
                } catch (RuntimeException e) {
                    LOGGER.error("Failed to load ore config file: {}", path, e);
                    throw e;
                }
            }
        }
        return townRules;
    }

    private static OreConfigFile readConfigFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            OreConfigFile config = GSON.fromJson(reader, OreConfigFile.class);
            return config == null ? new OreConfigFile() : config;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file: " + file, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON in file: " + file, e);
        }
    }

    private static List<OreDropRule> toRules(Path file, List<OreRuleJson> oreEntries) {
        if (oreEntries == null) {
            return List.of();
        }

        List<OreDropRule> rules = new ArrayList<>(oreEntries.size());
        for (int i = 0; i < oreEntries.size(); i++) {
            OreRuleJson entry = oreEntries.get(i);
            if (entry == null) {
                throw parseError(file, "ores[" + i + "] is null");
            }
            if (entry.orename == null || entry.orename.isBlank()) {
                throw parseError(file, "ores[" + i + "].orename is required");
            }
            if (entry.chance == null) {
                throw parseError(file, "ores[" + i + "].chance is required");
            }
            if (entry.chance < 0.0D || entry.chance > 1.0D) {
                throw parseError(file, "ores[" + i + "].chance must be between 0 and 1");
            }
            rules.add(new OreDropRule(entry.orename.trim(), entry.chance));
        }
        return rules;
    }

    private static void validateTotalChance(Path file, List<OreDropRule> rules) {
        double totalChance = rules.stream().mapToDouble(OreDropRule::chance).sum();
        if (totalChance > 1.0D) {
            throw parseError(file, "total chance must not be greater than 1");
        }
    }

    private static void writeDefaultFile(Path defaultFile) throws IOException {
        DefaultOreConfigFile template = new DefaultOreConfigFile();
        try (Writer writer = Files.newBufferedWriter(defaultFile, StandardCharsets.UTF_8)) {
            GSON.toJson(template, writer);
        }
    }

    private static IllegalArgumentException parseError(Path file, String message) {
        return new IllegalArgumentException(file + " " + message);
    }

    private static class OreConfigFile {
        String townid;
        List<OreRuleJson> ores = List.of();
    }

    private static class OreRuleJson {
        String orename;
        Double chance;
    }

    private static final class DefaultOreConfigFile extends OreConfigFile {
        DefaultOreConfigFile() {
            ores = List.of(rule("forge:ores/iron", 0.1D), rule("forge:ores/gold", 0.05D));
        }

        private static OreRuleJson rule(String oreName, double chance) {
            OreRuleJson rule = new OreRuleJson();
            rule.orename = oreName;
            rule.chance = chance;
            return rule;
        }
    }
}
