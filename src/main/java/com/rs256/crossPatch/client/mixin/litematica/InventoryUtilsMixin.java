package com.rs256.crossPatch.client.mixin.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import com.rs256.crossPatch.client.pickblock.PickRedirect;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

    /**
     * Applies the pickBlockPro {@code pickRedirect} map to schematic-world picks: when the
     * schematic block at {@code pos} has a redirect entry, the picked stack is replaced with the
     * target item before it is placed into the inventory.
     */
    @ModifyVariable(
            method = "schematicWorldPickBlock",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = false
    )
    private static ItemStack crosspatch$redirectSchematicPick(
            ItemStack stack,
            @Local(argsOnly = true) BlockPos pos,
            @Local(argsOnly = true) Level schematicWorld
    ) {
        if (schematicWorld == null || !PickRedirect.isEnabled()) {
            return stack;
        }

        Item target = PickRedirect.lookup(schematicWorld.getBlockState(pos).getBlock());
        return target != null ? new ItemStack(target) : stack;
    }
}
