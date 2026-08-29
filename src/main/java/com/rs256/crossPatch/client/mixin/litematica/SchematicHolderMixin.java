package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.cache.LitematicCache;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

/**
 * Serves {@code SchematicHolder#getOrLoad} misses from {@link LitematicCache}
 * instead of always re-reading the file, for the {@code optimizedLitematicLoading}
 * option.
 *
 * <p>The redirect deliberately sits on the {@code createFromFile} call rather
 * than at the head of {@code getOrLoad}: Litematica's own scan of its loaded
 * list runs first and still wins, so a schematic it already holds (including a
 * duplicate added by {@code addSchematic(schematic, true)} after a save) is
 * returned unchanged, and the caller keeps adding whatever comes back to that
 * list. The only thing that changes is where the instance comes from on a miss.
 */
@Mixin(targets = "fi.dy.masa.litematica.data.SchematicHolder")
public class SchematicHolderMixin {
    @Redirect(
            method = "getOrLoad",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/schematic/LitematicaSchematic;"
                            + "createFromFile(Ljava/nio/file/Path;Ljava/lang/String;Lfi/dy/masa/litematica/util/FileType;)"
                            + "Lfi/dy/masa/litematica/schematic/LitematicaSchematic;"
            ),
            remap = false
    )
    private LitematicaSchematic crosspatch$readThroughCache(Path dir, String fileName, FileType type) {
        // The same path getOrLoad matched its own list against, so a hit here is
        // a hit on exactly the file the caller asked for.
        Path file = dir.resolve(fileName);
        LitematicCache cache = LitematicCache.getInstance();
        LitematicaSchematic cached = cache.get(file);

        if (cached != null) {
            return cached;
        }

        LitematicaSchematic loaded = LitematicaSchematic.createFromFile(dir, fileName, type);

        if (loaded != null) {
            cache.put(file, loaded);
        }

        return loaded;
    }

    @Inject(method = "removeSchematic", at = @At("HEAD"), remap = false)
    private void crosspatch$evictFromCache(
            @Nullable LitematicaSchematic schematic,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LitematicCache.getInstance().remove(schematic);
    }
}
