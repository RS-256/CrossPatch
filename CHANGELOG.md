# v0.3.8

## Added

- support for Minecraft 26.2
- new option optimizedLitematicLoading (experimental): keep parsed .litematic files cached across a dimension change, instead of Litematica re-reading, version-checking and data-fixing every placed schematic from disk every time the world changes
- experimental options are now marked in the config GUI: the option name is drawn in red and a red warning is appended to the bottom of its hover text

## Changed

- requires Fabric Loader 0.19.5 or newer
- built against the latest malilib, litematica, itemscroller and tweakermore of each Minecraft version

## Removed

- autoCollectStackRoundUp, autoCollectWithShulker, autoCollectWithShulkerSingleItemOnly and the TweakerMore tab of the config GUI: **TweakerMore 3.33.2 introduced both features itself**, as autoCollectMaterialListItemRoundUpToStack and autoCollectMaterialListItemTakeShulkerBoxes. turn those on instead, the old CrossPatch values are not carried over
- note that TweakerMore's shulker collection only takes boxes holding a single required material that meet autoCollectMaterialListItemShulkerBoxFillThreshold, whose default of 1.0 means a full box only

## Fixed

- crash when joining a world with TweakerMore 3.33.2 or newer: the auto collect patch no longer had anything to inject into, which left TweakerMore's container processor permanently broken and threw on the first inventory packet
