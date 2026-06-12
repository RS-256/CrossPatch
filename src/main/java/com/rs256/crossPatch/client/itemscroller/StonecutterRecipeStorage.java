package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.CrossPatch;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.Constants;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.ListData;
import fi.dy.masa.malilib.util.data.tag.util.DataFileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import java.nio.file.Files;
import java.nio.file.Path;

public class StonecutterRecipeStorage {
    private static final int MAX_PAGES = 8;
    private static final int MAX_RECIPES = 18;
    private static final String FILE_NAME = "stonecutter_recipes.nbt";
    private static final StonecutterRecipeStorage INSTANCE = new StonecutterRecipeStorage(MAX_RECIPES * MAX_PAGES);

    private final StonecutterRecipePattern[] recipes;
    private int selected;
    private boolean dirty;

    public static StonecutterRecipeStorage getInstance() {
        return INSTANCE;
    }

    private StonecutterRecipeStorage(int recipeCount) {
        this.recipes = new StonecutterRecipePattern[recipeCount];
        this.initRecipes();
    }

    public void reset(boolean isLogout) {
        if (isLogout) {
            this.clearRecipes();
        }
    }

    private void initRecipes() {
        for (int i = 0; i < this.recipes.length; i++) {
            this.recipes[i] = new StonecutterRecipePattern();
        }
    }

    private void clearRecipes() {
        for (int i = 0; i < this.recipes.length; i++) {
            this.clearRecipe(i);
        }
    }

    public int getSelection() {
        return this.selected;
    }

    public void changeSelectedRecipe(int index) {
        if (index >= 0 && index < this.recipes.length) {
            this.selected = index;
            this.dirty = true;
        }
    }

    public void scrollSelection(boolean forward) {
        this.changeSelectedRecipe(this.selected + (forward ? 1 : -1));
    }

    public int getFirstVisibleRecipeId() {
        return this.getCurrentRecipePage() * this.getRecipeCountPerPage();
    }

    public int getTotalRecipeCount() {
        return this.recipes.length;
    }

    public int getRecipeCountPerPage() {
        return MAX_RECIPES;
    }

    public int getCurrentRecipePage() {
        return this.getSelection() / this.getRecipeCountPerPage();
    }

    public StonecutterRecipePattern getRecipe(int index) {
        if (index >= 0 && index < this.recipes.length) {
            return this.recipes[index];
        }

        return this.recipes[0];
    }

    public void storeRecipeToCurrentSelection(StonecutterScreen screen, Slot slot, boolean clearIfEmpty, Minecraft mc) {
        this.storeRecipe(this.getSelection(), screen, slot, clearIfEmpty, mc);
    }

    public void storeRecipe(int index, StonecutterScreen screen, Slot slot, boolean clearIfEmpty, Minecraft mc) {
        StonecutterRecipePattern recipe = this.getRecipe(index);

        if (slot == null || slot.index != 1) {
            return;
        }

        if (!slot.hasItem()) {
            if (clearIfEmpty) {
                recipe.clearRecipe();
                this.dirty = true;
            }
            return;
        }

        ItemStack ingredient = screen.getMenu().getSlot(0).getItem();
        ItemStack result = slot.getItem();

        if (ingredient.isEmpty() || result.isEmpty()) {
            return;
        }

        recipe.store(ingredient, result, null, null);

        this.dirty = true;
    }

    public void clearRecipe(int index) {
        this.getRecipe(index).clearRecipe();
        this.dirty = true;
    }

    private boolean isEmpty() {
        for (StonecutterRecipePattern recipe : this.recipes) {
            if (!recipe.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void readFromNBT(CompoundData data, RegistryAccess registry) {
        if (data == null || !data.contains("Recipes", Constants.NBT.TAG_LIST)) {
            return;
        }

        this.initRecipes();

        ListData tagList = data.getList("Recipes");
        int count = tagList.size();

        for (int i = 0; i < count; i++) {
            CompoundData tag = tagList.getCompoundAt(i);
            int index = tag.getByte("RecipeIndex");

            if (index >= 0 && index < this.recipes.length) {
                this.recipes[index].readFromData(tag, registry);

                if (tag.contains("LastNetworkId", Constants.NBT.TAG_INT)) {
                    RecipeDisplayId id = new RecipeDisplayId(tag.getInt("LastNetworkId"));
                    StonecutterRecipePattern recipe = this.recipes[index];
                    recipe.store(recipe.getIngredient(), recipe.getResult(), id, recipe.getRecipeCategory());
                }
            }
        }

        this.changeSelectedRecipe(data.getByte("Selected"));
        this.dirty = false;
    }

    private CompoundData writeToNBT(RegistryAccess registry) {
        CompoundData data = new CompoundData();

        if (this.isEmpty()) {
            return data;
        }

        ListData tagRecipes = new ListData();

        for (int i = 0; i < this.recipes.length; i++) {
            StonecutterRecipePattern entry = this.recipes[i];

            if (entry.isValid()) {
                CompoundData tag = entry.writeToData(registry);
                tag.putByte("RecipeIndex", (byte) i);
                tag.putString("RecipeType", "stonecutter");

                if (entry.getNetworkRecipeId() != null) {
                    tag.putInt("LastNetworkId", entry.getNetworkRecipeId().index());
                }

                tagRecipes.add(tag);
            }
        }

        data.put("Recipes", tagRecipes);
        data.putByte("Selected", (byte) this.selected);

        return data;
    }

    private Path getSaveDir() {
        //? if <=1.21.11 {
        /*return FileUtils.getMinecraftDirectoryAsPath().resolve("CrossPatch");
         *///?} else {
        return FileUtils.getMinecraftDirectory().resolve("CrossPatch");
        //?}
    }

    public void readFromDisk(RegistryAccess registry) {
        try {
            Path file = this.getSaveDir().resolve(FILE_NAME);

            if (Files.exists(file)) {
                CompoundData data = DataFileUtils.readCompoundDataFromNbtFile(file);

                if (data != null && !data.isEmpty()) {
                    this.readFromNBT(data, registry);
                }
            }
        } catch (Exception e) {
            CrossPatch.LOGGER.warn("readFromDisk(): Failed to read stonecutter recipes", e);
        }
    }

    public void writeToDisk(RegistryAccess registry) {
        if (!this.dirty) {
            return;
        }

        try {
            Path saveDir = this.getSaveDir();

            if (!Files.exists(saveDir)) {
                FileUtils.createDirectoriesIfMissing(saveDir);
            }

            if (Files.isDirectory(saveDir)) {
                Path fileTmp = saveDir.resolve(FILE_NAME + ".tmp");
                Path fileReal = saveDir.resolve(FILE_NAME);
                CompoundData data = this.writeToNBT(registry);

                if (data.isEmpty()) {
                    if (Files.exists(fileReal)) {
                        Files.delete(fileReal);
                    }

                    this.dirty = false;
                    return;
                }

                DataFileUtils.writeCompoundDataToCompressedNbtFile(fileTmp, data);

                if (Files.exists(fileReal)) {
                    Files.delete(fileReal);
                }

                Files.move(fileTmp, fileReal);
                this.dirty = false;
            }
        } catch (Exception e) {
            CrossPatch.LOGGER.warn("writeToDisk(): Failed to write stonecutter recipes", e);
        }
    }
}
