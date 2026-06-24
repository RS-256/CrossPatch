package com.rs256.crossPatch.client.itemscroller;

import fi.dy.masa.malilib.util.data.Constants;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.ListData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

/**
 * A stored anvil operation: the two input items (slot 0 and the optional slot 1
 * material) plus the resulting item. Mirrors {@link StonecutterRecipePattern},
 * but with two ingredients since the anvil result is fully determined by both
 * input slots.
 */
public class AnvilRecipePattern {
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack additional = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;

    public void clearRecipe() {
        this.input = ItemStack.EMPTY;
        this.additional = ItemStack.EMPTY;
        this.result = ItemStack.EMPTY;
    }

    public void store(ItemStack input, ItemStack additional, ItemStack result) {
        this.input = input.isEmpty() ? ItemStack.EMPTY : input.copy();
        this.additional = additional.isEmpty() ? ItemStack.EMPTY : additional.copy();
        this.result = result.isEmpty() ? ItemStack.EMPTY : result.copy();
    }

    public ItemStack getInput() {
        return this.input.isEmpty() ? ItemStack.EMPTY : this.input;
    }

    public ItemStack getAdditional() {
        return this.additional.isEmpty() ? ItemStack.EMPTY : this.additional;
    }

    public ItemStack getResult() {
        return this.result.isEmpty() ? ItemStack.EMPTY : this.result;
    }

    public boolean isEmpty() {
        return this.input.isEmpty() || this.result.isEmpty();
    }

    public boolean isValid() {
        return !this.result.isEmpty() && !this.input.isEmpty();
    }

    public void readFromData(CompoundData data, RegistryAccess registry) {
        this.clearRecipe();

        if (data.contains("Result", Constants.NBT.TAG_COMPOUND)) {
            this.result = fi.dy.masa.malilib.util.InventoryUtils.fromDataOrEmpty(registry, data.getCompound("Result"));
        }

        if (data.contains("Ingredients", Constants.NBT.TAG_LIST)) {
            ListData ingredients = data.getList("Ingredients");

            for (int i = 0; i < ingredients.size(); i++) {
                CompoundData tag = ingredients.getCompoundAt(i);
                int slot = tag.contains("Slot", Constants.NBT.TAG_INT) ? tag.getInt("Slot") : i;
                ItemStack stack = fi.dy.masa.malilib.util.InventoryUtils.fromDataOrEmpty(registry, tag);

                if (slot == 1) {
                    this.additional = stack;
                } else {
                    this.input = stack;
                }
            }
        }
    }

    public CompoundData writeToData(RegistryAccess registry) {
        CompoundData data = new CompoundData();

        if (this.isValid()) {
            data.put("Result", fi.dy.masa.malilib.util.InventoryUtils.toDataOrEmpty(this.result, registry));

            ListData ingredients = new ListData();

            if (!this.input.isEmpty()) {
                CompoundData tag = fi.dy.masa.malilib.util.InventoryUtils.toDataOrEmpty(this.input, registry);
                tag.putInt("Slot", 0);
                ingredients.add(tag);
            }

            if (!this.additional.isEmpty()) {
                CompoundData tag = fi.dy.masa.malilib.util.InventoryUtils.toDataOrEmpty(this.additional, registry);
                tag.putInt("Slot", 1);
                ingredients.add(tag);
            }

            data.put("Ingredients", ingredients);
        }

        return data;
    }
}
