# Changelog

## v0.2.12

Changes since v0.2.11.

### Added

- Added `layerChangeAmount`, which lets Litematica layer next/previous actions move multiple layers per key press. This applies to both CrossPatch Box Layer hotkeys and Litematica's own layer hotkeys.
- Added `pickBlockProPickShulkerWithItem`. In survival, when the picked item is not loose in the inventory but a carried shulker box contains it, pick block can bring that shulker box to hand instead.
- Added `pickBlockProPickSlotEnabled`, allowing pick-block placement to be restricted to selected hotbar slots. Slot lists such as `135`, `1,3,5`, and `1 3 5` are treated equivalently.
- Added `docs/options.md` with the full saved option and hotkey list.
- Added `MODRINTH.md` as release-page documentation.

### Changed

- Bumped the mod version from `0.2.11` to `0.2.12` in `stonecutter.properties.toml`.
- Replaced the Stonecutter template README with CrossPatch project documentation, including feature highlights and build commands.
- Updated licensing metadata to `LGPL-3.0-only` and refreshed the `LICENSE` text to match the integrated Pick Block Pro license.
- Registered the new Pick Block Pro and Litematica options in the config registry and English language file.

### Fixed

- Ensured the default for `forceLitematicaLayerAll` is `true`, so Litematica's render layer mode stays on `All` while CrossPatch Box Layer is active.
