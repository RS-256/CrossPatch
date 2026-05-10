package com.example;

import com.example.command.TemplateModCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
//? if <=1.18.2 {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?} else {
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
//?}

public class TemplateMod implements ModInitializer {
    public static final String MOD_ID = "template";
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "26.1";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    //? if <=1.18.2 {
    /*public static final Logger LOGGER = LogManager.getLogger();
     *///?} else {
    public static final Logger LOGGER = LogUtils.getLogger();
    //?}

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        //? if !release
        LOGGER.warn("I'm still a template!");

        //? if fapi: <0.100
        /*LOGGER.info("Fabric API is old on this version");*/

        registerCommands();
    }

    /**
     * Adapts to the ResourceLocation -> Identifier changes introduced in 1.21.
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new ResourceLocation(namespace, path);
        *///?} else {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?}
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TemplateModCommand.register(dispatcher, registryAccess));
    }
}