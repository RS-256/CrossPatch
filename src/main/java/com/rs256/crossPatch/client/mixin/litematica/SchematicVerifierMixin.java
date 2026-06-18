package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.util.BlockInfoListType;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Makes the schematic verifier's required-chunk set layer-aware.
 *
 * <p>litematica seeds {@code requiredChunks} from the whole placement
 * ({@code getTouchedChunks(ANY)}) and only drops a chunk once its full 3x3 client
 * neighbourhood plus the schematic chunk are loaded. In {@code RENDER_LAYERS} mode
 * {@code verifyChunk} only checks blocks inside the {@link LayerRange}, so chunks that lie
 * entirely outside the rendered region are never built/loaded, never removed, and linger
 * forever as "unseen" in the info HUD (and the verification never reaches the empty-set
 * completion). This filters those chunks out once, at start, so the verifier only requires
 * the chunks that will actually be rendered and verified.</p>
 *
 * <p>The effective rendered region mirrors {@code WorldRendererSchematicMixin}: the
 * intersection of litematica's {@link LayerRange} (when the verifier runs in
 * {@code RENDER_LAYERS} mode) and CrossPatch's 3-axis box layer (when enabled). A chunk is
 * kept only if at least one of its placement boxes still has volume after clipping to both.</p>
 *
 * <p>This is a start-time filter only: it does not follow later layer movements, matching the
 * one-time nature of litematica's own {@code requiredChunks} seeding.</p>
 *
 * <p>NOTE: {@code verifyChunk} itself is left untouched, so it still ignores CrossPatch's box
 * layer when deciding which blocks to compare. Chunks that only partially overlap the box may
 * therefore still report out-of-box blocks as mismatches; that is pre-existing behaviour and
 * out of scope for this unseen-chunk fix.</p>
 */
@Mixin(targets = "fi.dy.masa.litematica.schematic.verifier.SchematicVerifier")
public abstract class SchematicVerifierMixin {
    @Shadow(remap = false)
    private Set<ChunkPos> requiredChunks;

    @Shadow(remap = false)
    private int totalRequiredChunks;

    @Shadow(remap = false)
    private SchematicPlacement schematicPlacement;

    @Shadow(remap = false)
    public abstract void updateRequiredChunksStringList();

    @Inject(method = "startVerification", at = @At("TAIL"), remap = false)
    private void crosspatch$filterRequiredChunksByLayer(CallbackInfo ci) {
        boolean ranged = this.schematicPlacement.getSchematicVerifierType() == BlockInfoListType.RENDER_LAYERS;
        boolean boxEnabled = BoxLayerController.isEnabled();

        // No layer constraints active -> keep vanilla behaviour (require the whole placement).
        if (!ranged && !boxEnabled) {
            return;
        }

        LayerRange range = DataManager.getRenderLayerRange();
        Iterator<ChunkPos> iter = this.requiredChunks.iterator();

        while (iter.hasNext()) {
            if (!this.crosspatch$chunkIntersectsRenderedRegion(iter.next(), range, ranged)) {
                iter.remove();
            }
        }

        this.totalRequiredChunks = this.requiredChunks.size();
        this.updateRequiredChunksStringList();
    }

    @Unique
    private boolean crosspatch$chunkIntersectsRenderedRegion(ChunkPos pos, LayerRange range, boolean ranged) {
        Map<String, IntBoundingBox> boxes =
                this.schematicPlacement.getBoxesWithinChunk(crosspatch$chunkX(pos), crosspatch$chunkZ(pos));

        for (IntBoundingBox box : boxes.values()) {
            // Clip to the CrossPatch box layer first (returns the box unchanged when disabled,
            // null when there is no overlap), then test the litematica render layer.
            IntBoundingBox effective = BoxLayerController.clipBox(box);

            if (effective == null) {
                continue;
            }

            if (ranged && !range.intersects(effective)) {
                continue;
            }

            return true;
        }

        return false;
    }

    @Unique
    private static int crosspatch$chunkX(ChunkPos pos) {
        //? if <=1.21.11 {
        /*return pos.x;
        *///?} else {
        return pos.x();
        //?}
    }

    @Unique
    private static int crosspatch$chunkZ(ChunkPos pos) {
        //? if <=1.21.11 {
        /*return pos.z;
        *///?} else {
        return pos.z();
        //?}
    }
}
