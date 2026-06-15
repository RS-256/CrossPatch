# CrossPatch

CrossPatch is a completely client-side Fabric mod that adds small compatibility patches and
quality-of-life features around Litematica, ItemScroller, TweakerMore, and
pick-block behavior.

It is intended for players who already use Masa-style client mods and want a few
workflow gaps smoothed out without replacing those mods.

## Feature Highlights

- **Litematica Box Layer**: add independent X/Y/Z min/max render limits for schematic rendering, with hotkeys to move selected bounds.
- **Pick Block Pro**: This feature is based on a fork of [Pick Block Pro](https://modrinth.com/mod/pick-block-pro) at recent versions.
- **Stonecutter Mass Crafting**: store stonecutter recipes, refill the input slot, and craft repeatedly in an ItemScroller-style workflow.
- **TweakerMore Material Collection Patches**: round collected missing items up to full stacks and optionally collect matching shulker boxes from containers.

## Fork Inside

CrossPatch's `pickBlockPro` feature is a forked and integrated version of
[Pick Block Pro](https://modrinth.com/mod/pick-block-pro), adapted to live
inside CrossPatch alongside the other client-mod compatibility patches.

## Peak Options

These are the main options most users will probably care about first:

- `boxLayerEnabled`: enables CrossPatch's Litematica Box Layer render filter.
- `pickBlockPro`: enables pickBlockPro features.
- `massCraftStonecutter`: enables the stonecutter mass-crafting features.
- `autoCollectWithShulker`: lets TweakerMore collect whole shulker boxes that contain needed materials.

## Full Options

The complete option and hotkey list is maintained in the repository:

[docs/options.md](docs/options.md)

## Dependencies

### Required

- Fabric Loader
- Fabric API
- MaLiLib

### Suggested / Optional Integrations

CrossPatch can run without these mods, but its patch features are designed to
integrate with them:

- Mod Menu: opens the CrossPatch config screen from the Mod Menu interface.
- Litematica: enables Box Layer rendering controls and layer hotkey integration.
- ItemScroller: enables shared hotkey behavior and supports ItemScroller-style
  stonecutter crafting workflows.
- TweakerMore: enables the material-list auto-collect patches.
- Flashback: supported as an optional compatibility target.
- Bobby: supported as an optional compatibility target.

## License

CrossPatch is licensed under **LGPL-3.0-only**, matching the original
[Pick Block Pro](https://modrinth.com/mod/pick-block-pro) project that the
`pickBlockPro` feature is forked from.
