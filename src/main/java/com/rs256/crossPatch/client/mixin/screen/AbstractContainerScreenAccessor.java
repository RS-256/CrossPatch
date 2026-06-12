package com.rs256.crossPatch.client.mixin.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot crosspatch$getHoveredSlot();

    @Accessor("leftPos")
    int crosspatch$getLeftPos();
}
