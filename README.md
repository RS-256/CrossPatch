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

# Stonecutter Fabric template
## Setup
1. Review the supported Minecraft versions in `settings.gradle.kts`.
   For new entries, add `versions/.../gradle.properties` with the same keys as other versions.
2. Change `mod.group`, `mod.id` and `mod.name` properties in `gradle.properties`.
3. Rename `com.crossPatch` package in `src/main/java`.
4. Rename `src/main/resources/template.mixins.json` to use your mod's id.
5. Review the `LICENSE` file. 
   See the [license decision diagram](https://docs.codeberg.org/getting-started/licensing/#license-decision-diagram) for common options.
6. Review `src/main/resources/fabric.mod.json` to have up-to-date properties.

## Usage
- Use `"Set active project to ..."` Gradle tasks to update the Minecraft version
  available in `src/` classes.
- Use `buildAndCollect` Gradle task to store mod releases in `build/libs/`.
- Enable `mod-publish-plugin` in `stonecutter.gradle.kts` and `build.gradle.kts`
  and the corresponding code blocks to publish releases to Modrinth and Curseforge.
- Enable `maven-publish` in `build.gradle.kts` and the corresponding code block
  to publish releases to a personal maven repository.

## Useful links
- [Stonecutter beginner's guide](https://stonecutter.kikugie.dev/wiki/start/): *spoiler: you* ***need*** *to understand how it works!*
- [Fabric Discord server](https://discord.gg/v6v4pMv): for mod development help.
- [Stonecutter Discord server](https://discord.kikugie.dev/): for Stonecutter and Gradle help.
- [How To Ask Questions - the guide](http://www.catb.org/esr/faqs/smart-questions.html): also in [video form](https://www.youtube.com/results?search_query=How+To+Ask+Questions+The+Smart+Way).
