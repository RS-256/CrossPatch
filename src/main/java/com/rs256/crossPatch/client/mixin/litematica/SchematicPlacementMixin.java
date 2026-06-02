package com.rs256.crossPatch.client.mixin.litematica;

import com.google.common.collect.ImmutableMap;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.util.BlockInfoListType;
import fi.dy.masa.malilib.util.IntBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(targets = "fi.dy.masa.litematica.schematic.placement.SchematicPlacement")
public abstract class SchematicPlacementMixin {
    @Shadow(remap = false)
    public abstract BlockInfoListType getSchematicVerifierType();

    @Inject(
            method = "getBoxesWithinChunk(IILfi/dy/masa/litematica/schematic/placement/SubRegionPlacement$RequiredEnabled;)Lcom/google/common/collect/ImmutableMap;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void crosspatch$clipVerifierBoxesToBoxLayer(
            int chunkX,
            int chunkZ,
            SubRegionPlacement.RequiredEnabled required,
            CallbackInfoReturnable<ImmutableMap<String, IntBoundingBox>> cir
    ) {
        if (!BoxLayerController.isEnabled()
                || this.getSchematicVerifierType() != BlockInfoListType.RENDER_LAYERS) {
            return;
        }

        ImmutableMap<String, IntBoundingBox> boxes = cir.getReturnValue();
        Map<String, IntBoundingBox> clippedBoxes = new LinkedHashMap<>();

        for (Map.Entry<String, IntBoundingBox> entry : boxes.entrySet()) {
            IntBoundingBox clippedBox = BoxLayerController.clipBox(entry.getValue());

            if (clippedBox != null) {
                clippedBoxes.put(entry.getKey(), clippedBox);
            }
        }

        cir.setReturnValue(ImmutableMap.copyOf(clippedBoxes));
    }
}
