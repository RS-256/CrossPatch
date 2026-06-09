package com.rs256.crossPatch.client.event;

import com.rs256.crossPatch.client.config.Hotkeys;
import com.rs256.crossPatch.client.gui.GuiConfigs;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;

public final class KeyCallbacks {
    private KeyCallbacks() {
    }

    public static void init(Minecraft mc) {
        IHotkeyCallback callback = new BoxLayerHotkeyCallback(mc);

        Hotkeys.BOX_LAYER_NEXT.getKeybind().setCallback(callback);
        Hotkeys.BOX_LAYER_PREVIOUS.getKeybind().setCallback(callback);
        Hotkeys.BOX_LAYER_SET_HERE.getKeybind().setCallback(callback);
        Hotkeys.LAYER_AXIS_CYCLE.getKeybind().setCallback(callback);
        Hotkeys.LAYER_HOTKEY_CYCLE.getKeybind().setCallback(callback);
        Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback(callback);
    }

    private record BoxLayerHotkeyCallback(Minecraft mc) implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (this.mc.player == null || this.mc.level == null) {
                return false;
            }

            if (key == Hotkeys.BOX_LAYER_NEXT.getKeybind()) {
                BoxLayerController.next();
                return true;
            }

            if (key == Hotkeys.BOX_LAYER_PREVIOUS.getKeybind()) {
                BoxLayerController.previous();
                return true;
            }

            if (key == Hotkeys.BOX_LAYER_SET_HERE.getKeybind()) {
                BoxLayerController.setHere(this.mc);
                return true;
            }

            if (key == Hotkeys.LAYER_AXIS_CYCLE.getKeybind()) {
                BoxLayerController.cycleAxis();
                return true;
            }

            if (key == Hotkeys.LAYER_HOTKEY_CYCLE.getKeybind()) {
                BoxLayerController.cycleHotkeyAxis();
                return true;
            }

            if (key == Hotkeys.OPEN_CONFIG_GUI.getKeybind()) {
                GuiBase.openGui(new GuiConfigs(null));
                return true;
            }

            return false;
        }
    }
}
