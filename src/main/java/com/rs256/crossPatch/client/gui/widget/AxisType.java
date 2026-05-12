package com.rs256.crossPatch.client.gui.widget;

import net.minecraft.core.BlockPos;

public enum AxisType {
    X,
    Y,
    Z;

    public int get(BlockPos pos) {
        return switch (this) {
            case X -> pos.getX();
            case Y -> pos.getY();
            case Z -> pos.getZ();
        };
    }
}