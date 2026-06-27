package com.rs256.crossPatch.client.config;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ConfigRegistry {
    private ConfigRegistry() {
    }

    public static final List<TaggedConfig> ENTRIES = ImmutableList.of(
            /*
             * Generic CrossPatch hotkeys.
             */
            new TaggedConfig(
                    Hotkeys.OPEN_CONFIG_GUI,
                    ConfigTag.VIEWABLE,
                    ConfigTag.GENERIC,
                    ConfigTag.HOTKEY
            ),

            /*
             * CrossPatch - unique features
             */
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO_REACH_OVERRIDE,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO_PICK_PLAYER_HEAD,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO_PICK_SHULKER_WITH_ITEM,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO_PICK_SLOT_ENABLED,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.PickBlock.PICK_BLOCK_PRO_PICK_REDIRECT,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Hotkeys.OPEN_LITEMATIC_DOWNLOADER,
                    ConfigTag.VIEWABLE,
                    ConfigTag.CROSSPATCH,
                    ConfigTag.HOTKEY
            ),

            /*
             * ItemScroller - MassCraft options
             */
            new TaggedConfig(
                    Configs.ItemScroller.MASS_CRAFT_STONECUTTER,
                    ConfigTag.VIEWABLE,
                    ConfigTag.ITEMSCROLLER,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.ItemScroller.MASS_CRAFT_ANVIL,
                    ConfigTag.VIEWABLE,
                    ConfigTag.ITEMSCROLLER,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.ItemScroller.USE_ITEMSCROLLER_HOTKEYS,
                    ConfigTag.VIEWABLE,
                    ConfigTag.ITEMSCROLLER,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Hotkeys.RECIPE_VIEW,
                    ConfigTag.VIEWABLE,
                    ConfigTag.ITEMSCROLLER,
                    ConfigTag.HOTKEY
            ),
            new TaggedConfig(
                    Hotkeys.STORE_RECIPE,
                    ConfigTag.VIEWABLE,
                    ConfigTag.ITEMSCROLLER,
                    ConfigTag.HOTKEY
            ),

            /*
             * TweakerMore - patches
             */
            new TaggedConfig(
                    Configs.TweakerMore.AUTO_COLLECT_STACK_ROUND_UP,
                    ConfigTag.VIEWABLE,
                    ConfigTag.TWEAKERMORE,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER,
                    ConfigTag.VIEWABLE,
                    ConfigTag.TWEAKERMORE,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER_SINGLE_ITEM_ONLY,
                    ConfigTag.VIEWABLE,
                    ConfigTag.TWEAKERMORE,
                    ConfigTag.OPTION
            ),

            /*
             * Litematica - Box Layer options
             *
             * These are saved to the config file, but not shown in the normal config list.
             * They are edited through GuiLitematicaBoxLayer.
             */

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_X_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Y_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MIN_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MIN_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MIN_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MAX_ENABLED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MAX_SELECTED,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_Z_MAX_VALUE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),

            /*
             * Litematica - Box Layer hotkeys
             *
             * These are shown in the normal config list.
             */
            new TaggedConfig(
                    Configs.Litematica.BOX_LAYER_ENABLED,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.USE_LITEMATICA_LAYER_HOTKEYS,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.FORCE_LITEMATICA_LAYER_ALL,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.LAYER_CHANGE_AMOUNT,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.PICK_BLOCK_SHULKER_PREFER_FEWER,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.RENDER_ENTITIES_AS_TRANSLUCENT,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.RENDER_ENTITIES_TRANSLUCENT_ALPHA,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.RENDER_ANIMATIVE_BLOCK_AS_TRANSLUCENT,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.OPTION
            ),
            new TaggedConfig(
                    Configs.Litematica.RENDER_ANIMATIVE_BLOCK_TRANSLUCENT_ALPHA,
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
            ),
            new TaggedConfig(
                    Hotkeys.LAYER_AXIS_CYCLE,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.HOTKEY
            ),
            new TaggedConfig(
                    Hotkeys.LAYER_HOTKEY_CYCLE,
                    ConfigTag.VIEWABLE,
                    ConfigTag.LITEMATICA,
                    ConfigTag.HOTKEY
            )
    );
}
