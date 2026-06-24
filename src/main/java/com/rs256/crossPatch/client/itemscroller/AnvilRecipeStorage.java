package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.CrossPatch;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.Constants;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.ListData;
import fi.dy.masa.malilib.util.data.tag.util.DataFileUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stores anvil operations selected from the recipe view, mirroring
 * {@link StonecutterRecipeStorage}.
 */
public class AnvilRecipeStorage {
    private static final int MAX_PAGES = 8;
    private static final int MAX_RECIPES = 18;
    private static final String FILE_NAME = "anvil_recipes.nbt";
    private static final AnvilRecipeStorage INSTANCE = new AnvilRecipeStorage(MAX_RECIPES * MAX_PAGES);

    private final AnvilRecipePattern[] recipes;
    private int selected;
    private boolean dirty;

    public static AnvilRecipeStorage getInstance() {
        return INSTANCE;
    }

    private AnvilRecipeStorage(int recipeCount) {
        this.recipes = new AnvilRecipePattern[recipeCount];
        this.initRecipes();
    }

    public void reset(boolean isLogout) {
        if (isLogout) {
            this.clearRecipes();
        }
    }

    private void initRecipes() {
        for (int i = 0; i < this.recipes.length; i++) {
            this.recipes[i] = new AnvilRecipePattern();
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

    public AnvilRecipePattern getRecipe(int index) {
        if (index >= 0 && index < this.recipes.length) {
            return this.recipes[index];
        }

        return this.recipes[0];
    }

    /**
     * Captures the current anvil contents (both input slots and the result) into
     * the selected recipe slot. Clears the slot if there is nothing to store.
     */
    public void storeRecipeToCurrentSelection(AnvilMenu menu, boolean clearIfEmpty) {
        AnvilRecipePattern recipe = this.getRecipe(this.getSelection());

        ItemStack input = menu.getSlot(0).getItem();
        ItemStack additional = menu.getSlot(1).getItem();
        ItemStack result = menu.getSlot(2).getItem();

        if (input.isEmpty() || result.isEmpty()) {
            if (clearIfEmpty) {
                recipe.clearRecipe();
                this.dirty = true;
            }
            return;
        }

        recipe.store(input, additional, result);
        this.dirty = true;
    }

    public void clearRecipe(int index) {
        this.getRecipe(index).clearRecipe();
        this.dirty = true;
    }

    private boolean isEmpty() {
        for (AnvilRecipePattern recipe : this.recipes) {
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
            AnvilRecipePattern entry = this.recipes[i];

            if (entry.isValid()) {
                CompoundData tag = entry.writeToData(registry);
                tag.putByte("RecipeIndex", (byte) i);
                tag.putString("RecipeType", "anvil");
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
            CrossPatch.LOGGER.warn("readFromDisk(): Failed to read anvil recipes", e);
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
            CrossPatch.LOGGER.warn("writeToDisk(): Failed to write anvil recipes", e);
        }
    }
}
