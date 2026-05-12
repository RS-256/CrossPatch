package com.rs256.crossPatch.client.config;

import fi.dy.masa.malilib.config.IConfigBase;

import java.util.Collections;
import java.util.EnumSet;

public record TaggedConfig(
        IConfigBase config,
        EnumSet<ConfigTag> tags
) {
    public TaggedConfig(IConfigBase config, ConfigTag first, ConfigTag... rest) {
        this(config, makeTags(first, rest));
    }

    private static EnumSet<ConfigTag> makeTags(ConfigTag first, ConfigTag... rest) {
        EnumSet<ConfigTag> set = EnumSet.of(first);

        Collections.addAll(set, rest);

        return set;
    }

    public boolean has(ConfigTag tag) {
        return this.tags.contains(tag);
    }

    public boolean hasAll(ConfigTag... requiredTags) {
        for (ConfigTag tag : requiredTags) {
            if (!this.has(tag)) {
                return false;
            }
        }

        return true;
    }
}