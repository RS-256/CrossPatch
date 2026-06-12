package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class StonecutterWorldLoadListener implements IWorldLoadListener {
    @Override
    public void onWorldLoadPre(ClientLevel worldBefore, ClientLevel worldAfter, Minecraft mc) {
        if (worldBefore != null && worldAfter == null && Configs.ItemScroller.MASS_CRAFT_STONECUTTER.getBooleanValue()) {
            StonecutterRecipeStorage.getInstance().writeToDisk(worldBefore.registryAccess());
        }
    }

    @Override
    public void onWorldLoadPost(ClientLevel worldBefore, ClientLevel worldAfter, Minecraft mc) {
        StonecutterRecipeStorage.getInstance().reset(worldAfter == null);

        if (worldBefore == null && worldAfter != null && Configs.ItemScroller.MASS_CRAFT_STONECUTTER.getBooleanValue()) {
            StonecutterRecipeStorage.getInstance().readFromDisk(worldAfter.registryAccess());
        }
    }
}
