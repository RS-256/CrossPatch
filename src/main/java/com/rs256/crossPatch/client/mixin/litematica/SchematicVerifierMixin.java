package com.rs256.crossPatch.client.mixin.litematica;

import com.google.common.collect.ImmutableMap;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.util.BlockInfoListType;
import fi.dy.masa.malilib.util.IntBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(targets = "fi.dy.masa.litematica.schematic.verifier.SchematicVerifier")
public abstract class SchematicVerifierMixin {
    @Shadow(remap = false)
    private SchematicPlacement schematicPlacement;

    @Redirect(
            method = "verifyChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/schematic/placement/SchematicPlacement;getBoxesWithinChunk(II)Lcom/google/common/collect/ImmutableMap;",
                    remap = false
            ),
            remap = false
    )
    private ImmutableMap<String, IntBoundingBox> crosspatch$clipVerifierBoxesToBoxLayer(
            SchematicPlacement placement,
            int chunkX,
            int chunkZ
    ) {
        ImmutableMap<String, IntBoundingBox> boxes = placement.getBoxesWithinChunk(chunkX, chunkZ);

        if (!BoxLayerController.isEnabled()
                || this.schematicPlacement.getSchematicVerifierType() != BlockInfoListType.RENDER_LAYERS) {
            return boxes;
        }

        Map<String, IntBoundingBox> clippedBoxes = new LinkedHashMap<>();

        for (Map.Entry<String, IntBoundingBox> entry : boxes.entrySet()) {
            IntBoundingBox clippedBox = BoxLayerController.clipBox(entry.getValue());

            if (clippedBox != null) {
                clippedBoxes.put(entry.getKey(), clippedBox);
            }
        }

        return ImmutableMap.copyOf(clippedBoxes);
    }
}
