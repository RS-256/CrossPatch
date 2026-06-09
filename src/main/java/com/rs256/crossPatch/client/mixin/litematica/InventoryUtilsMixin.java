package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fi.dy.masa.litematica.util.InventoryUtils")
public class InventoryUtilsMixin {
    @Inject(
            method = "schematicWorldPickBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void crosspatch$filterBoxLayerPickBlock(
            ItemStack stack,
            BlockPos pos,
            Level schematicWorld,
            Minecraft mc,
            CallbackInfo ci
    ) {
        if (!BoxLayerController.shouldRender(pos)) {
            ci.cancel();
        }
    }
}
