package com.rs256.crossPatch.client.event;

import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.config.Hotkeys;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import com.rs256.crossPatch.client.litematica.layer.LayerRepeatTimer;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.util.GuiUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * Hold-to-repeat for the box layer hotkeys. malilib hotkey callbacks only fire on the
 * initial key press, so like Litematica's LayerUtils this polls the held state every
 * client tick and keeps moving the layer while the key stays down.
 */
public class BoxLayerRepeatHandler implements IClientTickHandler {
    private static final BoxLayerRepeatHandler INSTANCE = new BoxLayerRepeatHandler();

    // The box layer feature (and BoxLayerController's refresh path) requires Litematica.
    private final boolean litematicaLoaded = FabricLoader.getInstance().isModLoaded("litematica");
    private final LayerRepeatTimer timer = new LayerRepeatTimer();
    private boolean savePending;

    private BoxLayerRepeatHandler() {
    }

    public static BoxLayerRepeatHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onClientTick(Minecraft mc) {
        if (!this.litematicaLoaded) {
            return;
        }

        if (mc.player == null || mc.level == null || GuiUtils.getCurrentScreen() != null) {
            this.stopRepeat();
            return;
        }

        int direction = heldDirection();

        if (direction == 0) {
            this.stopRepeat();
            return;
        }

        if (this.timer.shouldStep(direction)) {
            BoxLayerController.offsetSelectedBounds(direction * BoxLayerController.layerChangeAmount(), false);
            this.savePending = true;
        }
    }

    private void stopRepeat() {
        this.timer.reset();

        if (this.savePending) {
            this.savePending = false;
            Configs.saveToFile();
        }
    }

    private static int heldDirection() {
        boolean nextHeld = Hotkeys.BOX_LAYER_NEXT.getKeybind().isKeybindHeld();
        boolean prevHeld = Hotkeys.BOX_LAYER_PREVIOUS.getKeybind().isKeybindHeld();

        if (BoxLayerController.shouldUseLitematicaLayerHotkeys()) {
            nextHeld |= fi.dy.masa.litematica.config.Hotkeys.LAYER_NEXT.getKeybind().isKeybindHeld();
            prevHeld |= fi.dy.masa.litematica.config.Hotkeys.LAYER_PREVIOUS.getKeybind().isKeybindHeld();
        }

        return nextHeld ? 1 : (prevHeld ? -1 : 0);
    }
}
