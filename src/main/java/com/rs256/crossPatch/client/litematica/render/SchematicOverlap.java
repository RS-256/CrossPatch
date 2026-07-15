package com.rs256.crossPatch.client.litematica.render;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;

/**
 * Support code for the {@code renderOverlappingSchematics} option. The option
 * itself is applied by {@link com.rs256.crossPatch.client.mixin.litematica.WorldPlacingUtilsMixin}
 * while Litematica (re)places a schematic-world chunk; this class only makes a
 * toggle take effect immediately by re-placing every already-built chunk.
 * A plain {@code SchematicWorldRefresher.updateAll()} is not enough for that,
 * because it re-renders the existing schematic world without re-placing the
 * placements into it.
 */
public final class SchematicOverlap {
    private SchematicOverlap() {
    }

    /**
     * Only call when Litematica is loaded — this touches Litematica classes.
     */
    public static void init() {
        Configs.Litematica.RENDER_OVERLAPPING_SCHEMATICS.setValueChangeCallback(
                config -> rebuildAllPlacements()
        );
    }

    private static void rebuildAllPlacements() {
        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();

        for (SchematicPlacement placement : manager.getAllSchematicsPlacements()) {
            manager.markChunksForRebuild(placement);
        }
    }
}
