package com.rs256.crossPatch.client.mixin.screen;

import com.rs256.crossPatch.client.itemscroller.StonecutterRenderEventHandler;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.client.Minecraft;
//? if <=1.21.11 {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    //? if <=1.21.11 {
    /*@Inject(method = "renderWithTooltipAndSubtitles", at = @At("TAIL"))
    private void crosspatch_onDrawScreenPost(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        StonecutterRenderEventHandler.instance().onDrawScreenPost(GuiContext.fromGuiGraphics(graphics), Minecraft.getInstance(), mouseX, mouseY);
    }
    *///?} else {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void crosspatch_onDrawScreenPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        StonecutterRenderEventHandler.instance().onDrawScreenPost(GuiContext.fromGuiGraphics(graphics), Minecraft.getInstance(), mouseX, mouseY);
    }
    //?}
}
