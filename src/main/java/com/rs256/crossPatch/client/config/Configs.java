package com.rs256.crossPatch.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.Reference;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";

    public static class Generic {
        private static final String TRANSLATION_PREFIX = Reference.MOD_ID + ".config.generic";

        public static final ConfigBoolean BOX_LAYER_ENABLED =
                new ConfigBoolean("boxLayerEnabled", false)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean USE_LITEMATICA_LAYER_HOTKEYS =
                new ConfigBoolean("useLitematicaLayerHotkeys", true)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean FORCE_LITEMATICA_LAYER_ALL =
                new ConfigBoolean("forceLitematicaLayerAll", true)
                        .apply(TRANSLATION_PREFIX);

        /**
         * Number of layers a single Layer Next / Layer Previous press moves. Applies
         * both to Litematica's own layer hotkeys and to CrossPatch Box Layer. A value
         * of 1 is the normal single-step behaviour; higher values treat one press as
         * that many presses.
         */
        public static final ConfigInteger LAYER_CHANGE_AMOUNT =
                new ConfigInteger("layerChangeAmount", 1, 1, 30_000_000)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_X_MIN_ENABLED =
                new ConfigBoolean("boxLayerXMinEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_X_MIN_SELECTED =
                new ConfigBoolean("boxLayerXMinSelected", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_X_MIN_VALUE =
                new ConfigInteger("boxLayerXMinValue", 0, -30_000_000, 30_000_000)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_X_MAX_ENABLED =
                new ConfigBoolean("boxLayerXMaxEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_X_MAX_SELECTED =
                new ConfigBoolean("boxLayerXMaxSelected", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_X_MAX_VALUE =
                new ConfigInteger("boxLayerXMaxValue", 0, -30_000_000, 30_000_000)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_Y_MIN_ENABLED =
                new ConfigBoolean("boxLayerYMinEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_Y_MIN_SELECTED =
                new ConfigBoolean("boxLayerYMinSelected", true)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_Y_MIN_VALUE =
                new ConfigInteger("boxLayerYMinValue", 0, -2048, 2048)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_Y_MAX_ENABLED =
                new ConfigBoolean("boxLayerYMaxEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_Y_MAX_SELECTED =
                new ConfigBoolean("boxLayerYMaxSelected", true)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_Y_MAX_VALUE =
                new ConfigInteger("boxLayerYMaxValue", 0, -2048, 2048)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_Z_MIN_ENABLED =
                new ConfigBoolean("boxLayerZMinEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_Z_MIN_SELECTED =
                new ConfigBoolean("boxLayerZMinSelected", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_Z_MIN_VALUE =
                new ConfigInteger("boxLayerZMinValue", 0, -30_000_000, 30_000_000)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean BOX_LAYER_Z_MAX_ENABLED =
                new ConfigBoolean("boxLayerZMaxEnabled", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigBoolean BOX_LAYER_Z_MAX_SELECTED =
                new ConfigBoolean("boxLayerZMaxSelected", false)
                        .apply(TRANSLATION_PREFIX);
        public static final ConfigInteger BOX_LAYER_Z_MAX_VALUE =
                new ConfigInteger("boxLayerZMaxValue", 0, -30_000_000, 30_000_000)
                        .apply(TRANSLATION_PREFIX);
    }

    public static class PickBlock {
        private static final String TRANSLATION_PREFIX = Reference.MOD_ID + ".config.crosspatch";

        /**
         * Master switch for the pickBlockPro feature. When disabled, vanilla pick
         * block behaviour is left untouched.
         */
        public static final ConfigBoolean PICK_BLOCK_PRO =
                new ConfigBoolean("pickBlockPro", false)
                        .apply(TRANSLATION_PREFIX);

        /**
         * Maximum distance (in blocks) at which pick block is allowed to target a
         * block when {@link #PICK_BLOCK_PRO} is enabled. Vanilla pick block is
         * limited to 4 blocks; this override extends that reach.
         */
        public static final ConfigDouble PICK_BLOCK_PRO_REACH_OVERRIDE =
                new ConfigDouble("pickBlockProReachOverride", 4.0, 1.0, 128.0, true)
                        .apply(TRANSLATION_PREFIX);

        /**
         * When enabled, picking while looking at a player yields that player's current
         * player head (with their skin), instead of vanilla's behaviour. Works at normal
         * reach; combined with {@link #PICK_BLOCK_PRO} it also picks distant players.
         */
        public static final ConfigBoolean PICK_BLOCK_PRO_PICK_PLAYER_HEAD =
                new ConfigBoolean("pickBlockProPickPlayerHead", false)
                        .apply(TRANSLATION_PREFIX);

        /**
         * Survival only. When enabled, if the picked item is not loose in the inventory but a
         * shulker box in the inventory contains a matching item, that shulker box is brought to
         * hand instead. The shulker's contents are left untouched - only the box is picked. In
         * creative the item itself is picked as usual (the shulker is left alone).
         */
        public static final ConfigBoolean PICK_BLOCK_PRO_PICK_SHULKER_WITH_ITEM =
                new ConfigBoolean("pickBlockProPickShulkerWithItem", false)
                        .apply(TRANSLATION_PREFIX);

        /**
         * Restricts which hotbar slots pick may place items into. The value lists the allowed
         * slot numbers (1-9); any non-digit separators are ignored, so "135", "1,3,5" and
         * "1 3 5" are equivalent. Empty (or no valid digits) means no restriction - any slot
         * may be used. When set, picked items only ever land in the listed slots.
         */
        public static final ConfigString PICK_BLOCK_PRO_PICK_SLOT_ENABLED =
                new ConfigString("pickBlockProPickSlotEnabled", "")
                        .apply(TRANSLATION_PREFIX);
    }

    public static class ItemScroller {
        private static final String TRANSLATION_PREFIX = Reference.MOD_ID + ".config.itemscroller";

        public static final ConfigBoolean MASS_CRAFT_STONECUTTER =
                new ConfigBoolean("massCraftStonecutter", false)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean MASS_CRAFT_ANVIL =
                new ConfigBoolean("massCraftAnvil", false)
                        .apply(TRANSLATION_PREFIX);

        public static final ConfigBoolean USE_ITEMSCROLLER_HOTKEYS =
                new ConfigBoolean("useItemScrollerHotkeys", true)
                        .apply(TRANSLATION_PREFIX);
    }

    public static class TweakerMore {
        private static final String TRANSLATION_PREFIX = Reference.MOD_ID + ".config.tweakermore";

        /**
         * When enabled, TweakerMore's "auto collect material list item" feature
         * collects each material rounded up to the next full stack instead of the
         * exact missing amount. For example, if 14 items are missing and the stack
         * size is 64, it collects 64; if the stack size is 16, it collects 16.
         */
        public static final ConfigBoolean AUTO_COLLECT_STACK_ROUND_UP =
                new ConfigBoolean("autoCollectStackRoundUp", false)
                        .apply(TRANSLATION_PREFIX);

        /**
         * When enabled, TweakerMore's "auto collect material list item" feature also
         * pulls whole shulker boxes (any color) out of the container when their
         * contents match a missing material. Litematica counts the contents of
         * shulker boxes carried in the player inventory, so the collected box is
         * automatically credited against the material list.
         */
        public static final ConfigBoolean AUTO_COLLECT_WITH_SHULKER =
                new ConfigBoolean("autoCollectWithShulker", false)
                        .apply(TRANSLATION_PREFIX);

        /**
         * Restricts {@link #AUTO_COLLECT_WITH_SHULKER} to shulker boxes that are filled
         * with a single item type (matching the needed material). When disabled, any
         * shulker box that merely contains the needed item is collected.
         */
        public static final ConfigBoolean AUTO_COLLECT_WITH_SHULKER_SINGLE_ITEM_ONLY =
                new ConfigBoolean("autoCollectWithShulkerSingleItemOnly", true)
                        .apply(TRANSLATION_PREFIX);
    }

    public static void loadFromFile() {
        //? if <=1.21.11 {
        /*Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
         *///?} else {
        Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);
        //?}

        if (!Files.exists(configFile)) {
            saveToFile();
        }

        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "Generic", ConfigQuery.allOptions());
                ConfigUtils.readConfigBase(root, "Hotkeys", ConfigQuery.allHotkeys());

                CrossPatch.LOGGER.info(
                        "loadFromFile(): Successfully loaded config file '{}'.",
                        configFile.toAbsolutePath()
                );
            } else {
                CrossPatch.LOGGER.error(
                        "loadFromFile(): Failed to load config file '{}'.",
                        configFile.toAbsolutePath()
                );
            }
        }
    }

    public static void saveToFile() {
        //? if <=1.21.11 {
        /*Path dir = FileUtils.getConfigDirectoryAsPath();
         *///?} else {
        Path dir = FileUtils.getConfigDirectory();
        //?}

        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);

            CrossPatch.LOGGER.info(
                    "saveToFile(): Creating directory '{}'.",
                    dir.toAbsolutePath()
            );
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Generic", ConfigQuery.allOptions());
            ConfigUtils.writeConfigBase(root, "Hotkeys", ConfigQuery.allHotkeys());

            JsonUtils.writeJsonToFile(root, dir.resolve(CONFIG_FILE_NAME));
        } else {
            CrossPatch.LOGGER.error(
                    "saveToFile(): Config folder '{}' does not exist!",
                    dir.toAbsolutePath()
            );
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }
}
