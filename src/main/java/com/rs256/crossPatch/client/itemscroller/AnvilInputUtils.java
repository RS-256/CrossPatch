package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.malilib.util.GuiUtils;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

/**
 * Recipe-view helpers for the anvil, sharing the ItemScroller hotkey integration
 * with {@link StonecutterInputUtils}.
 */
public final class AnvilInputUtils {
    private AnvilInputUtils() {
    }

    public static boolean isRecipeViewOpen() {
        return Configs.ItemScroller.MASS_CRAFT_ANVIL.getBooleanValue() &&
                StonecutterInputUtils.craftingFeaturesEnabled() &&
                GuiUtils.getCurrentScreen() instanceof AnvilScreen &&
                StonecutterInputUtils.isRecipeViewKeybindHeld();
    }
}
