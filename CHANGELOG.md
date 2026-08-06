# v0.3.6

## Added

- new option renderOverlappingSchematics: render every schematic placement in areas where their bounds overlap, instead of only one of them
- new hotkey layerChangeAmountModifier: hold it and scroll the mouse wheel to raise/lower layerChangeAmount in game

## Fixed

- some options metadata are re-considered the actual code 
- another fix of not working placementRestriction when the boxLayer is enabled
- pickBlockProPickSlotEnabled no longer moves an item that is already on your hotbar into a listed slot; it is now selected in place like vanilla, and the restriction only applies to items brought in from elsewhere