package com.rs256.crossPatch.client.event;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.config.Hotkeys;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

public class InputHandler implements IKeybindProvider {
    private static final InputHandler INSTANCE = new InputHandler();

    private InputHandler() {
    }

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey hotkey : Hotkeys.HOTKEY_LIST) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(
                Reference.MOD_NAME,
                Reference.MOD_ID + ".hotkeys.category.box_layer",
                Hotkeys.HOTKEY_LIST
        );
    }
}