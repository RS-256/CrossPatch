package com.rs256.crossPatch.client.mixin.flashback;

import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = IntegratedServer.class, priority = 500)
public abstract class ReplayServerViewDistanceMixin {
    @ModifyArg(
            method = "tickServer",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            index = 1
    )
    private int crosspatch$useClientRenderDistanceForFlashbackReplay(int viewDistance) {
        if (!((Object) this instanceof ReplayServer)) {
            return viewDistance;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return viewDistance;
        }

        int clientRenderDistance = minecraft.options.getEffectiveRenderDistance();
        return Math.max(viewDistance, clientRenderDistance);
    }
}
