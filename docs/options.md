# Options

CrossPatch saved options are written to `config/crosspatch.json`.

The normal config GUI shows entries tagged as viewable in `ConfigRegistry`.
Some Litematica Box Layer values are saved options too, but are edited from the
Box Layer GUI instead of the normal option list. Hotkeys listed in
`Hotkeys.HOTKEY_LIST` are available to malilib's keybind manager; most of them
are also saved through `ConfigRegistry`.

## CrossPatch

| Option | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `pickBlockPro` | boolean | `false` | - | Enables CrossPatch pick-block handling. When disabled, vanilla pick-block behavior is left untouched. |
| `pickBlockProReachOverride` | double | `4.0` | `1.0` to `128.0` | Maximum block pick distance while `pickBlockPro` is enabled. Vanilla pick block is limited to normal block interaction reach. |
| `pickBlockProPickPlayerHead` | boolean | `false` | - | Makes pick block return the targeted player's current player head. Works at normal reach by itself, and at extended reach when `pickBlockPro` is also enabled. |

## Generic

| Option | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `openConfigGui` | hotkey | unbound | - | Opens the CrossPatch config GUI. |

## Litematica

| Option | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `boxLayerEnabled` | boolean | `false` | - | Enables CrossPatch Box Layer filtering for Litematica schematic rendering. When enabled, schematic blocks outside the active X/Y/Z bounds are hidden. |
| `useLitematicaLayerHotkeys` | boolean | `true` | - | Lets Litematica's own layer next/previous/set-here hotkeys drive the CrossPatch Box Layer when `boxLayerEnabled` is on. |
| `forceLitematicaLayerAll` | boolean | `true` | - | Keeps Litematica's own render layer mode set to `All` while CrossPatch Box Layer is active. |
| `layerChangeAmount` | integer | `1` | `1` to `30000000` | Number of layers moved by one layer next/previous action. Applies to Litematica layer hotkeys and CrossPatch Box Layer hotkeys. |
| `boxLayerNext` | hotkey | unbound | - | Moves the selected Box Layer bounds forward by `layerChangeAmount`. |
| `boxLayerPrevious` | hotkey | unbound | - | Moves the selected Box Layer bounds backward by `layerChangeAmount`. |
| `boxLayerSetHere` | hotkey | unbound | - | Sets the selected Box Layer bounds to the player's current block position. |
| `layerAxisCycle` | hotkey | unbound | - | Cycles Litematica's layer axis. When Box Layer is enabled, also cycles which Box Layer bounds are enabled between X/Y/Z. |
| `layerHotkeyCycle` | hotkey | unbound | - | Cycles which Box Layer bounds are selected for hotkey movement between X/Y/Z and prints the current selection. |

### Litematica Box Layer GUI Options

These values are saved in the config file, but are edited through the
Litematica Box Layer GUI or the inline Litematica layer GUI controls.

For each axis bound:

| Option pattern | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `boxLayer<Axis><Min/Max>Enabled` | boolean | `false` | - | Enables that bound as an active render limit. Disabled bounds do not clip rendering. |
| `boxLayer<Axis><Min/Max>Selected` | boolean | X/Z: `false`, Y: `true` | - | Marks that bound as controlled by Box Layer next/previous/set-here hotkeys. |
| `boxLayer<Axis><Min/Max>Value` | integer | `0` | X/Z: `-30000000` to `30000000`; Y: `-2048` to `2048` | Coordinate value for that bound. Min bounds hide blocks below the value; max bounds hide blocks above the value. |

Concrete saved option names:

| Axis | Min enabled | Min selected | Min value | Max enabled | Max selected | Max value |
| --- | --- | --- | --- | --- | --- | --- |
| X | `boxLayerXMinEnabled` | `boxLayerXMinSelected` | `boxLayerXMinValue` | `boxLayerXMaxEnabled` | `boxLayerXMaxSelected` | `boxLayerXMaxValue` |
| Y | `boxLayerYMinEnabled` | `boxLayerYMinSelected` | `boxLayerYMinValue` | `boxLayerYMaxEnabled` | `boxLayerYMaxSelected` | `boxLayerYMaxValue` |
| Z | `boxLayerZMinEnabled` | `boxLayerZMinSelected` | `boxLayerZMinValue` | `boxLayerZMaxEnabled` | `boxLayerZMaxSelected` | `boxLayerZMaxValue` |

## ItemScroller

| Option | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `massCraftStonecutter` | boolean | `false` | - | Enables stonecutter mass crafting support. With a stored recipe, the feature can refill the input slot, select the recipe, craft everything, or mass-craft while the mass-craft hotkey is held. |
| `massCraftAnvil` | boolean | `false` | - | Enables anvil mass crafting. Place the items in the anvil and hold the mass-craft hotkey: the feature keeps taking the result and refilling both input slots with matching items (e.g. `diamond_sword` + `enchanted_book`), applying the same operation across a whole inventory. Hold the recipe-view hotkey to show stored anvil recipes on the left and store/select one (store hotkey or click); the selected recipe is used for refilling, otherwise the items currently in the slots are used. The rename box no longer auto-focuses (so the hotkeys are not swallowed as typed text): click it to type a name, then press Enter to hand the keyboard back to the hotkeys. |
| `useItemScrollerHotkeys` | boolean | `true` | - | When ItemScroller is installed, uses ItemScroller's recipe view, store recipe, craft everything, and mass craft hotkeys/settings. When disabled or ItemScroller is absent, CrossPatch fallback hotkeys are used where available. |
| `recipeView` | hotkey | unbound | - | Holds open CrossPatch's stonecutter / anvil recipe view when ItemScroller hotkeys are not being used. |
| `storeRecipe` | hotkey | unbound | - | Stores a recipe while the recipe view is open: the stonecutter recipe under the mouse, or the current anvil contents. |
| `craftEverything` | hotkey | unbound | - | Crafts as much as possible from the stored stonecutter recipe into the player inventory. This hotkey is registered with malilib's keybind manager, but is not currently registered in `ConfigRegistry`, so it is not shown in the normal config list and is not saved by CrossPatch's config handler. |

## TweakerMore

| Option | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `autoCollectStackRoundUp` | boolean | `false` | - | Changes TweakerMore's auto collect material list item feature to collect each missing material rounded up to a full stack. For example, 14 missing items with max stack size 64 collects 64. |
| `autoCollectWithShulker` | boolean | `false` | - | Lets TweakerMore's auto collect material list item feature also move whole shulker boxes from the container when their contents include a missing material. Litematica then counts the shulker contents in the player inventory. |
| `autoCollectWithShulkerSingleItemOnly` | boolean | `true` | - | Restricts `autoCollectWithShulker` to shulker boxes containing only one item type, matching the needed material. When disabled, any shulker box containing the needed material may be collected. |
