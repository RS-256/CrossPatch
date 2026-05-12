package com.rs256.crossPatch.client.config;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ConfigRegistry {
    private ConfigRegistry() {
    }

    public static final List<TaggedConfig> ENTRIES = ImmutableList.of(
            /*
             * Litematica - Box Layer options
             *
             * These are saved to the config file, but not shown in the normal config list.
             * They are edited through GuiLitematicaBoxLayer.
             */

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_X_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Y_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_Z_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            /*
             * Litematica - Box Layer hotkeys
             *
             * These are shown in the normal config list.
             */
            new TaggedConfig(
                    Configs.Generic.BOX_LAYER_ENABLED,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Generic.USE_LITEMATICA_LAYER_HOTKEYS,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Hotkeys.BOX_LAYER_NEXT,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.HOTKEY
            ),
            new TaggedConfig(
                    Hotkeys.BOX_LAYER_PREVIOUS,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.HOTKEY
            ),
            new TaggedConfig(
                    Hotkeys.BOX_LAYER_SET_HERE,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.HOTKEY
            )
    );
}