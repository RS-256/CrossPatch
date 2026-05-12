package com.rs256.crossPatch.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.Reference;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
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

    public static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);

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
        Path dir = FileUtils.getConfigDirectory();

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