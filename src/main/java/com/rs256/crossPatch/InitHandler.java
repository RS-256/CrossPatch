package com.rs256.crossPatch;

import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.event.InputHandler;
import com.rs256.crossPatch.client.event.KeyCallbacks;
import com.rs256.crossPatch.client.itemscroller.AnvilMassCraftHandler;
import com.rs256.crossPatch.client.itemscroller.AnvilWorldLoadListener;
import com.rs256.crossPatch.client.itemscroller.StonecutterMassCraftHandler;
import com.rs256.crossPatch.client.itemscroller.StonecutterWorldLoadListener;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.minecraft.client.Minecraft;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());

        StonecutterWorldLoadListener listener = new StonecutterWorldLoadListener();
        WorldLoadHandler.getInstance().registerWorldLoadPreHandler(listener);
        WorldLoadHandler.getInstance().registerWorldLoadPostHandler(listener);

        AnvilWorldLoadListener anvilListener = new AnvilWorldLoadListener();
        WorldLoadHandler.getInstance().registerWorldLoadPreHandler(anvilListener);
        WorldLoadHandler.getInstance().registerWorldLoadPostHandler(anvilListener);

        TickHandler.getInstance().registerClientTickHandler(StonecutterMassCraftHandler.getInstance());
        TickHandler.getInstance().registerClientTickHandler(AnvilMassCraftHandler.getInstance());

        KeyCallbacks.init(Minecraft.getInstance());
    }
}
