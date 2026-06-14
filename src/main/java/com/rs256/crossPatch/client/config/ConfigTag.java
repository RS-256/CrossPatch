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
