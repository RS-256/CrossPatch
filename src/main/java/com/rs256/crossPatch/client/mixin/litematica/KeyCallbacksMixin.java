package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "fi.dy.masa.litematica.event.KeyCallbacks$KeyCallbackHotkeys")
public class KeyCallbacksMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "onKeyAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void crosspatch$handleBoxLayerHotkeys(
            KeyAction action,
            IKeybind key,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.mc.player == null || this.mc.level == null) {
            return;
        }

        if (BoxLayerController.shouldUseLitematicaLayerHotkeys()) {
            if (key == Hotkeys.LAYER_NEXT.getKeybind()) {
                BoxLayerController.next();
                cir.setReturnValue(true);
                return;
            }

            if (key == Hotkeys.LAYER_PREVIOUS.getKeybind()) {
                BoxLayerController.previous();
                cir.setReturnValue(true);
                return;
            }

            if (key == Hotkeys.LAYER_SET_HERE.getKeybind()) {
                BoxLayerController.setHere(this.mc);
                cir.setReturnValue(true);
            }

            return;
        }

        // Litematica's own layer hotkeys move its render layer by a single step.
        // Scale that to layerChangeAmount; amount == 1 keeps vanilla Litematica behaviour.
        int amount = BoxLayerController.layerChangeAmount();

        if (amount == 1) {
            return;
        }

        if (key == Hotkeys.LAYER_NEXT.getKeybind()) {
            DataManager.getRenderLayerRange().moveLayer(amount);
            cir.setReturnValue(true);
            return;
        }

        if (key == Hotkeys.LAYER_PREVIOUS.getKeybind()) {
            DataManager.getRenderLayerRange().moveLayer(-amount);
            cir.setReturnValue(true);
        }
    }
}