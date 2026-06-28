package com.rs256.crossPatch.client.config;

import fi.dy.masa.malilib.config.IConfigBase;

import java.util.Collections;
import java.util.EnumSet;

public record TaggedConfig(
        IConfigBase config,
        EnumSet<ConfigTag> tags,
        ConfigMeta meta
) {
    public TaggedConfig(IConfigBase config, ConfigTag first, ConfigTag... rest) {
        this(config, makeTags(first, rest), ConfigMeta.EMPTY);
    }

    private static EnumSet<ConfigTag> makeTags(ConfigTag first, ConfigTag... rest) {
        EnumSet<ConfigTag> set = EnumSet.of(first);

        Collections.addAll(set, rest);

        return set;
    }

    /**
     * Returns a copy of this entry that additionally requires the given mods to be
     * present. Surfaced in the hover as a checklist; does not change behaviour.
     */
    public TaggedConfig required(String... mods) {
        return new TaggedConfig(this.config, this.tags, this.meta.withRequired(mods));
    }

    /**
     * Returns a copy of this entry that additionally suggests the given mods as
     * optional companions. Surfaced in the hover as a checklist; does not change
     * behaviour.
     */
    public TaggedConfig suggested(String... mods) {
        return new TaggedConfig(this.config, this.tags, this.meta.withSuggested(mods));
    }

    /**
     * Returns a copy of this entry that links to the given related configs. Each
     * reference is a config name (bare for the same group, or {@code group.name} to
     * cross groups); its display name is resolved from the lang files at render time.
     */
    public TaggedConfig seeAlso(String... refs) {
        return new TaggedConfig(this.config, this.tags, this.meta.withSeeAlso(refs));
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
