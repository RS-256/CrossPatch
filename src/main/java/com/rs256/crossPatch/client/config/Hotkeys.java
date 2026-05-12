package com.rs256.crossPatch.client.config;

import com.google.common.collect.ImmutableList;
import com.rs256.crossPatch.Reference;
import fi.dy.masa.malilib.config.options.ConfigHotkey;

import java.util.List;

public class Hotkeys {
    private static final String TRANSLATION_PREFIX = Reference.MOD_ID + ".config.hotkeys";

    public static final ConfigHotkey BOX_LAYER_NEXT =
            new ConfigHotkey("boxLayerNext", "")
                    .apply(TRANSLATION_PREFIX);

    public static final ConfigHotkey BOX_LAYER_PREVIOUS =
            new ConfigHotkey("boxLayerPrevious", "")
                    .apply(TRANSLATION_PREFIX);

    public static final ConfigHotkey BOX_LAYER_SET_HERE =
            new ConfigHotkey("boxLayerSetHere", "")
                    .apply(TRANSLATION_PREFIX);

    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            BOX_LAYER_NEXT,
            BOX_LAYER_PREVIOUS,
            BOX_LAYER_SET_HERE
    );
}