# CrossPatch

CrossPatch is a client-side Fabric mod that adds small compatibility patches and
quality-of-life features around Litematica, ItemScroller, TweakerMore, and
pick-block behavior.

Current features include:

- Litematica Box Layer rendering limits with configurable X/Y/Z bounds.
- Extended pick-block behavior, including optional player-head picking.
- Stonecutter mass-crafting support for ItemScroller-style workflows.
- TweakerMore material collection patches, including stack rounding and shulker
  box collection support.

For the full list of available options and hotkeys, see
[docs/options.md](docs/options.md).

## License

CrossPatch is licensed under **LGPL-3.0-only**, matching the original
[Pick Block Pro](https://modrinth.com/mod/pick-block-pro) project that the
`pickBlockPro` feature is forked from.

## Development
### stonecutter template fabric
This project uses
[stonecutter-template-fabric](https://github.com/RS-256/stonecutter-template-fabric)
to keep one source tree working across multiple Minecraft targets.

Supported targets are configured in [settings.gradle.kts](settings.gradle.kts) (must provide properties at [stonecutter.properties.toml](stonecutter.properties.toml)).
The active development target is set in [stonecutter.gradle.kts](stonecutter.gradle.kts)
with `stonecutter active`.

Common development commands:

```sh
./gradlew runClientCurrentVersion
```

Runs the Fabric client for the currently activated mc version.

```sh
./gradlew build
```

Builds the active version.

## Build

Release builds are collected under `build/libs/<mod version>/`.

```sh
./gradlew buildReleaseRemapped
```

Builds remapped jars for the configured release versions.

Per-version build helpers are also available:

```sh
./gradlew :<minecraft-version>:buildAndCollect
./gradlew :<minecraft-version>:buildAndCollectRemapped
./gradlew :<minecraft-version>:buildAndCollectSources
```

Example:

```sh
./gradlew :1.21.11:buildAndCollectRemapped
```
