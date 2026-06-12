package com.rs256.crossPatch.client.itemscroller;

import fi.dy.masa.malilib.util.data.Constants;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.ListData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public class StonecutterRecipePattern {
    private ItemStack ingredient = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private RecipeDisplayId networkRecipeId;
    private RecipeBookCategory category;

    public void clearRecipe() {
        this.ingredient = ItemStack.EMPTY;
        this.result = ItemStack.EMPTY;
        this.networkRecipeId = null;
        this.category = null;
    }

    public void store(ItemStack ingredient, ItemStack result, RecipeDisplayId networkRecipeId, RecipeBookCategory category) {
        this.ingredient = ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.copy();
        this.result = result.isEmpty() ? ItemStack.EMPTY : result.copy();
        this.networkRecipeId = networkRecipeId;
        this.category = category;
    }

    public ItemStack getIngredient() {
        return this.ingredient.isEmpty() ? ItemStack.EMPTY : this.ingredient;
    }

    public ItemStack getResult() {
        return this.result.isEmpty() ? ItemStack.EMPTY : this.result;
    }

    public ItemStack[] getRecipeItems() {
        return new ItemStack[]{this.getIngredient()};
    }

    public int getRecipeLength() {
        return 1;
    }

    public RecipeDisplayId getNetworkRecipeId() {
        return this.networkRecipeId;
    }

    public RecipeBookCategory getRecipeCategory() {
        return this.category;
    }

    public boolean isEmpty() {
        return this.ingredient.isEmpty() || this.result.isEmpty();
    }

    public boolean isValid() {
        return !this.result.isEmpty();
    }

    public void readFromData(CompoundData data, RegistryAccess registry) {
        this.clearRecipe();

        if (data.contains("Result", Constants.NBT.TAG_COMPOUND)) {
            this.result = fi.dy.masa.malilib.util.InventoryUtils.fromDataOrEmpty(registry, data.getCompound("Result"));
        }

        if (data.contains("Ingredients", Constants.NBT.TAG_LIST)) {
            ListData ingredients = data.getList("Ingredients");

            if (!ingredients.isEmpty()) {
                this.ingredient = fi.dy.masa.malilib.util.InventoryUtils.fromDataOrEmpty(registry, ingredients.getCompoundAt(0));
            }
        }
    }

    public CompoundData writeToData(RegistryAccess registry) {
        CompoundData data = new CompoundData();

        if (this.isValid()) {
            data.putInt("Length", this.getRecipeLength());
            data.put("Result", fi.dy.masa.malilib.util.InventoryUtils.toDataOrEmpty(this.result, registry));

            ListData ingredients = new ListData();

            if (!this.ingredient.isEmpty()) {
                CompoundData tag = fi.dy.masa.malilib.util.InventoryUtils.toDataOrEmpty(this.ingredient, registry);
                tag.putInt("Slot", 0);
                ingredients.add(tag);
            }

            data.put("Ingredients", ingredients);
        }

        return data;
    }
}
