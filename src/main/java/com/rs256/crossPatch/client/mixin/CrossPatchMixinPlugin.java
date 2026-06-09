package com.rs256.crossPatch.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CrossPatchMixinPlugin implements IMixinConfigPlugin {
    private static final String LITEMATICA_MIXIN_PACKAGE =
            "com.rs256.crossPatch.client.mixin.litematica.";
    private static final String FLASHBACK_MIXIN_PACKAGE =
            "com.rs256.crossPatch.client.mixin.flashback.";

    private boolean litematicaLoaded;
    private boolean flashbackLoaded;
    private boolean bobbyLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        this.litematicaLoaded = fabricLoader.isModLoaded("litematica");
        this.flashbackLoaded = fabricLoader.isModLoaded("flashback");
        this.bobbyLoaded = fabricLoader.isModLoaded("bobby");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(LITEMATICA_MIXIN_PACKAGE)) {
            return this.litematicaLoaded;
        }
        if (mixinClassName.startsWith(FLASHBACK_MIXIN_PACKAGE)) {
            return this.flashbackLoaded && this.bobbyLoaded;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }
}
