package com.rs256.crossPatch.client.mixin.screen;

import com.rs256.crossPatch.client.config.Configs;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Frees the anvil rename text field from grabbing the keyboard while CrossPatch
 * mass anvil is enabled.
 *
 * <p>The vanilla anvil auto-focuses the rename box on open and re-focuses it
 * every time the input item changes ({@code slotChanged} for slot 0), and the
 * box cannot lose focus. That swallows the mass-craft hotkeys as typed text. With
 * mass anvil on, the box behaves like a normal click-to-focus field: it never
 * auto-focuses, so the hotkeys work; click it to type a name and press Enter (see
 * {@code InputHandler}) to release the keyboard again.</p>
 */
@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @Shadow
    private EditBox name;

    @Inject(method = "subInit", at = @At("TAIL"))
    private void crosspatch_freeNameFieldFocus(CallbackInfo ci) {
        if (Configs.ItemScroller.MASS_CRAFT_ANVIL.getBooleanValue() && this.name != null) {
            // Let the rename box give the keyboard back and start unfocused.
            this.name.setCanLoseFocus(true);
            this.name.setFocused(false);
        }
    }

    @Inject(method = "setInitialFocus()V", at = @At("HEAD"), cancellable = true)
    private void crosspatch_skipInitialNameFocus(CallbackInfo ci) {
        if (Configs.ItemScroller.MASS_CRAFT_ANVIL.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "slotChanged", at = @At("TAIL"))
    private void crosspatch_keepNameUnfocused(AbstractContainerMenu menu, int slotIndex, ItemStack stack, CallbackInfo ci) {
        // Vanilla re-focuses the rename box whenever the input item (slot 0)
        // changes, including the automatic refills during mass crafting. Undo that
        // so the keyboard stays free; the user clicks the box when they want to type.
        if (slotIndex == 0 && Configs.ItemScroller.MASS_CRAFT_ANVIL.getBooleanValue() && this.name != null) {
            this.name.setFocused(false);
            ((Screen) (Object) this).setFocused(null);
        }
    }
}
