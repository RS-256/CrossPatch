# Changelog

## v0.3.0
### Added
- Added `pickBlockProPickRedirect`, a Pick Block Pro option that redirects picked blocks to another item. Entries can be written as `source -> target`, `source = target`, or `source target`; for example, `minecraft:grass_block -> minecraft:dirt`.
- Applied Pick Block Pro redirects to Litematica schematic-world pick block, so redirected schematic blocks now pick the configured replacement item.
- Applied `pickBlockProReachOverride` to Litematica schematic-world pick block ray traces, allowing distant schematic blocks to be picked while Pick Block Pro is enabled.
### Changed
- Reorganized saved options into mod-specific config categories: `Litematica`, `PickBlock`, `ItemScroller`, and `TweakerMore`, instead of writing all normal options under `Generic`.
- Existing option values saved under the old `Generic` category are not migrated automatically, so affected settings will be reset to their defaults after updating.
- Renamed the internal Litematica option group from `Generic` to `Litematica` and updated its translation keys.
- Added hover/comment text for Litematica, ItemScroller, TweakerMore, and hotkey options in the config GUI.
### Fixed
- Fixed the config GUI title showing a stale hard-coded CrossPatch version by reading the installed mod version from Fabric Loader metadata.
- Fixed missing hover text for several config and hotkey entries.
- Fixed Pick Block Pro features not being applied consistently when picking blocks from Litematica's schematic world.
