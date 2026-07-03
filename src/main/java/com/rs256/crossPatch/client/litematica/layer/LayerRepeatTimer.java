package com.rs256.crossPatch.client.litematica.layer;

/**
 * Hold-to-repeat timing matching Litematica's layer hotkeys:
 * after an initial delay, steps fire at a fixed interval while the key is held.
 * The initial key press itself is handled by the hotkey callback, not by this timer.
 */
public final class LayerRepeatTimer {
    private static final long REPEAT_DELAY_MS = 400;
    private static final long REPEAT_INTERVAL_MS = 50;

    private int direction;
    private long holdStartTime;
    private long lastStepTime;

    /**
     * @param currentDirection the held direction: 1, -1, or 0 when released
     * @return true when a repeat step should fire
     */
    public boolean shouldStep(int currentDirection) {
        if (currentDirection == 0) {
            this.direction = 0;
            return false;
        }

        long now = System.currentTimeMillis();

        if (this.direction != currentDirection) {
            this.direction = currentDirection;
            this.holdStartTime = now;
            this.lastStepTime = now;
            return false;
        }

        if (now - this.holdStartTime >= REPEAT_DELAY_MS && now - this.lastStepTime >= REPEAT_INTERVAL_MS) {
            this.lastStepTime = now;
            return true;
        }

        return false;
    }

    public void reset() {
        this.direction = 0;
    }
}
