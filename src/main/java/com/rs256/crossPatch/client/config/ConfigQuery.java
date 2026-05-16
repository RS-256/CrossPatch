package com.rs256.crossPatch.client.config;

import fi.dy.masa.malilib.config.IConfigBase;

import java.util.List;

public final class ConfigQuery {
    private ConfigQuery() {
    }

    /**
     * All normal options to be saved under "Generic".
     * Includes internal options that are not shown in the normal config GUI.
     */
    public static List<IConfigBase> allOptions() {
        return collectByTag(ConfigTag.OPTION);
    }

    /**
     * All hotkeys to be saved under "Hotkeys".
     */
    public static List<IConfigBase> allHotkeys() {
        return collectByTag(ConfigTag.HOTKEY);
    }

    /**
     * All entries visible in the normal config GUI.
     */
    public static List<IConfigBase> viewableAll() {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(ConfigTag.VIEWABLE))
                .sorted(ConfigQuery::compareOptionThenHotkey)
                .map(TaggedConfig::config)
                .toList();
    }

    /**
     * Visible entries for a specific mod tab.
     * Example: LITEMATICA tab shows VIEWABLE + LITEMATICA entries.
     */
    public static List<IConfigBase> viewableFor(ConfigTag modTag) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(ConfigTag.VIEWABLE))
                .filter(entry -> entry.has(modTag))
                .sorted(ConfigQuery::compareOptionThenHotkey)
                .map(TaggedConfig::config)
                .toList();
    }

    private static List<IConfigBase> collectByTag(ConfigTag tag) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(tag))
                .map(TaggedConfig::config)
                .toList();
    }

    private static int compareOptionThenHotkey(TaggedConfig a, TaggedConfig b) {
        int typeCompare = Integer.compare(typeOrder(a), typeOrder(b));

        if (typeCompare != 0) {
            return typeCompare;
        }

        return a.config().getName().compareToIgnoreCase(b.config().getName());
    }

    private static int typeOrder(TaggedConfig entry) {
        if (entry.has(ConfigTag.OPTION)) {
            return 0;
        }

        if (entry.has(ConfigTag.HOTKEY)) {
            return 1;
        }

        return 2;
    }
}
