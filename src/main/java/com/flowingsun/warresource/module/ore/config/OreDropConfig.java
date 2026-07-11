package com.flowingsun.warresource.module.ore.config;

import java.util.List;
import java.util.Map;

public record OreDropConfig(List<OreDropRule> defaultRules, Map<String, List<OreDropRule>> townRules) {
    public OreDropConfig {
        defaultRules = List.copyOf(defaultRules);
        townRules = Map.copyOf(townRules);
    }

    public static OreDropConfig empty() {
        return new OreDropConfig(List.of(), Map.of());
    }

    public List<OreDropRule> rulesForTown(String townId) {
        if (townId != null && !townId.isBlank()) {
            List<OreDropRule> rules = townRules.get(townId);
            if (rules != null) {
                return rules;
            }
        }
        return defaultRules;
    }
}
