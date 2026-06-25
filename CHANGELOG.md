# Changelog

## v0.3.3
### Added
- Added `massCraftAnvil`, an ItemScroller-style anvil mass crafting option.
### Changed
- Updated `recipeView` and `storeRecipe` hotkey descriptions to cover both stonecutter and anvil recipes.
- Changed the anvil rename box behavior while `massCraftAnvil` is enabled so it no longer auto-focuses and swallows hotkeys. Click the field to type a name, then press Enter to return focus to hotkeys.
- Updated Gradle wrapper metadata.

## v0.3.2
### Fixed
- Made Litematica schematic verification account for render-layer limits, so verification only requires chunks that intersect the currently rendered region.
- Fixed Litematica verifier progress getting stuck on unseen chunks outside the active render layer or Box Layer bounds.
- Fixed Box Layer mode still blocking pick/raycast behavior on hidden schematic blocks by making world-level schematic reads see hidden positions as air.
- Fixed the schematic verifier's incremental changed-position checks being affected by Box Layer hiding; verifier checks now read the raw schematic state where needed.
- Fixed TweakerMore shulker auto-collect taking a shulker box that would over-collect when `autoCollectStackRoundUp` is disabled.
- Fixed TweakerMore shulker collection compatibility with newer Minecraft inventory click APIs.
### Changed
- Updated Gradle wrapper metadata.
