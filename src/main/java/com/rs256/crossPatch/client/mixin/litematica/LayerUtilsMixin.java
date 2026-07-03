package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import com.rs256.crossPatch.client.litematica.layer.LayerRepeatTimer;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.data.DataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// LayerUtils only exists since Litematica 0.26.6 / 0.27.4 (the versions that added
// hold-to-repeat), which is newer than the compile-time dependency. @Pseudo skips
// this mixin silently on older Litematica, where there is no repeat to override.
@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.util.LayerUtils", remap = false)
public class LayerUtilsMixin {
    @Unique
    private static final LayerRepeatTimer crosspatch$scaledRepeatTimer = new LayerRepeatTimer();

    @Inject(method = "layerChangeRepeatTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void crosspatch$overrideLayerChangeRepeat(CallbackInfo ci) {
        // Box layer mode owns Litematica's layer hotkeys; BoxLayerRepeatHandler
        // repeats them against the box layer instead of the render layer.
        if (BoxLayerController.shouldUseLitematicaLayerHotkeys()) {
            crosspatch$scaledRepeatTimer.reset();
            ci.cancel();
            return;
        }

        int amount = BoxLayerController.layerChangeAmount();

        if (amount == 1) {
            // Vanilla Litematica behaviour, let the original repeat run.
            crosspatch$scaledRepeatTimer.reset();
            return;
        }

        // Same scaling as KeyCallbacksMixin applies to the initial key press.
        boolean nextHeld = Hotkeys.LAYER_NEXT.getKeybind().isKeybindHeld();
        boolean prevHeld = Hotkeys.LAYER_PREVIOUS.getKeybind().isKeybindHeld();
        int direction = nextHeld ? 1 : (prevHeld ? -1 : 0);

        if (crosspatch$scaledRepeatTimer.shouldStep(direction)) {
            DataManager.getRenderLayerRange().moveLayer(direction * amount);
        }

        ci.cancel();
    }
}
