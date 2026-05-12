package com.rs256.crossPatch;

import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.event.InputHandler;
import com.rs256.crossPatch.client.event.KeyCallbacks;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.minecraft.client.Minecraft;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        KeyCallbacks.init(Minecraft.getInstance());
    }
}