package com.example.client.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ExampleClientMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void init(CallbackInfo info) {
        // This code is injected into the start of ClientPacketListener.handleLogin(...)V
    }
}
