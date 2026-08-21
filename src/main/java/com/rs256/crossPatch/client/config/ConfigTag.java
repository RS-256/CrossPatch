package com.rs256.crossPatch.client.config;

public enum ConfigTag {
    /**
     * Shown in the normal malilib config list.
     * Internal values used only by custom GUIs should NOT have this tag.
     */
    VIEWABLE,

    /**
     * Normal config option.
     */
    OPTION,

    /**
     * Hotkey config.
     */
    HOTKEY,

    /**
     * Unfinished or not fully validated. Shown in the config GUI with a red
     * option name and a red warning appended to the bottom of the hover text.
     */
    EXPERIMENTAL,

    /**
     * Only meaningful to someone who knows what it changes internally. Purely a
     * marker for now: it carries no rendering or behaviour of its own.
     */
    ADVANCED,

    /**
     * Related to Litematica patches.
     */
    LITEMATICA,

    /**
     * Related to ItemScroller patches.
     */
    ITEMSCROLLER,

    /**
     * Generic CrossPatch settings.
     */
    GENERIC,

    /**
     * CrossPatch's own unique features (not patches to other mods).
     */
    CROSSPATCH,

    /**
     * Related to TweakerMore patches.
     */
    TWEAKERMORE,

    /**
     * Reserved for future Tweakeroo patches.
     */
    //TWEAKEROO,

    /**
     * Reserved for future MiniHUD patches.
     */
    //MINIHUD
}
