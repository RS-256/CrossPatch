package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.gui.litematica.GuiLitematicaBoxLayer;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fi.dy.masa.litematica.gui.GuiRenderLayer")
public abstract class GuiRenderLayerMixin {
    @ModifyConstant(
            method = "initGui",
            constant = @Constant(intValue = 60),
            remap = false
    )
    private int crosspatch$moveLayerControlsDown(int original) {
        return original + 26;
    }

    @Inject(method = "initGui", at = @At("RETURN"), remap = false)
    private void crosspatch$addBoxLayerButton(CallbackInfo ci) {
        GuiBase gui = (GuiBase) (Object) this;
        Screen screen = (Screen) (Object) this;

        int x = 10;
        int y = 60;
        int buttonWidth = 90;

        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                buttonWidth,
                20,
                StringUtils.translate(Reference.MOD_ID + ".gui.button.open_litematica_box_layer_short")
        );

        gui.addButton(
                button,
                (buttonBase, mouseButton) -> GuiBase.openGui(new GuiLitematicaBoxLayer(screen))
        );
    }
}