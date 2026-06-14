package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(targets = "fi.dy.masa.malilib.gui.GuiRenderLayerEditBase")
public abstract class GuiRenderLayerEditBaseMixin {
    @Inject(
            method = "createLayerConfigButton",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void crosspatch$disableLayerModeButtonWhenForcedAll(
            int x,
            int y,
            @Coerce Object type,
            LayerRange layerRange,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (!crosspatch$isModeButton(type)) {
            return;
        }

        if (!BoxLayerController.shouldForceLitematicaLayerAll()) {
            return;
        }

        BoxLayerController.forceLitematicaLayerAll();

        String label = crosspatch$getDisplayName(type, layerRange);

        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                -1,
                20,
                label,
                new String[0]
        );

        button.setEnabled(false);

        ((GuiBase) (Object) this).addButton(
                button,
                (buttonBase, mouseButton) -> {
                }
        );

        cir.setReturnValue(button.getWidth() + 2);
    }

    @Unique
    private static boolean crosspatch$isModeButton(Object type) {
        if (type instanceof Enum<?> enumValue) {
            return "MODE".equals(enumValue.name());
        }

        return false;
    }

    @Unique
    private static String crosspatch$getDisplayName(Object type, LayerRange layerRange) {
        try {
            Method method = type.getClass().getDeclaredMethod("getDisplayName", LayerRange.class);
            method.setAccessible(true);

            Object result = method.invoke(type, layerRange);

            if (result instanceof String string) {
                return string;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return "Layers: All";
    }
}