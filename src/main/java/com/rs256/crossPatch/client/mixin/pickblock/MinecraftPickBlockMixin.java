package com.rs256.crossPatch.client.mixin.pickblock;

import com.rs256.crossPatch.client.pickblock.PickBlockProHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks vanilla pick block so the {@code pickBlockPro} feature can extend its reach.
 *
 * <p>The pick block method was renamed between Minecraft versions
 * ({@code pickBlock} on 1.21.11, {@code pickBlockOrEntity} on 26.1.2); the body is
 * otherwise identical. Only the {@code method} reference differs, switched by
 * Stonecutter below.</p>
 *
 * @see PickBlockProHandler
 */
@Mixin(Minecraft.class)
public class MinecraftPickBlockMixin {
    @Inject(
            //? if <=1.21.11 {
            /*method = "pickBlock",
            *///?} else {
            method = "pickBlockOrEntity",
            //?}
            at = @At("HEAD"),
            cancellable = true
    )
    private void crosspatch$pickBlockPro(CallbackInfo ci) {
        if (PickBlockProHandler.tryHandlePick((Minecraft) (Object) this)) {
            ci.cancel();
        }
    }
}
