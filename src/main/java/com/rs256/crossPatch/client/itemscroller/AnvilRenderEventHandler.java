package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.mixin.screen.AbstractContainerScreenAccessor;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the stored anvil recipe list on the left of the anvil screen, mirroring
 * {@link StonecutterRenderEventHandler} but previewing both input items.
 */
public class AnvilRenderEventHandler {
    private static final AnvilRenderEventHandler INSTANCE = new AnvilRenderEventHandler();

    private final Minecraft mc = Minecraft.getInstance();
    private int recipeListX;
    private int recipeListY;
    private int recipesPerColumn;
    private int columnWidth;
    private int columns;
    private int numberTextWidth;
    private int gapColumn;
    private int entryHeight;
    private double scale;

    public static AnvilRenderEventHandler instance() {
        return INSTANCE;
    }

    public void onDrawScreenPost(GuiContext ctx, Minecraft mc, int mouseX, int mouseY) {
        this.renderRecipeView(ctx, mc, mouseX, mouseY);

        if (GuiUtils.getCurrentScreen() instanceof AnvilScreen gui && AnvilInputUtils.isRecipeViewOpen()) {
            AnvilRecipeStorage recipes = AnvilRecipeStorage.getInstance();
            int recipeId = this.getHoveredRecipeId(mouseX, mouseY, recipes, gui);

            ctx.pose().pushMatrix();

            if (recipeId >= 0) {
                this.renderHoverTooltip(ctx, mouseX, mouseY, recipes.getRecipe(recipeId));
            } else {
                ItemStack stack = this.getHoveredRecipeIngredient(mouseX, mouseY, recipes.getRecipe(recipes.getSelection()));

                if (!stack.isEmpty()) {
                    InventoryOverlay.renderStackToolTip(ctx, mouseX, mouseY, stack);
                }
            }

            ctx.pose().popMatrix();
        }
    }

    private void renderRecipeView(GuiContext ctx, Minecraft mc, int mouseX, int mouseY) {
        if (GuiUtils.getCurrentScreen() instanceof AnvilScreen gui && AnvilInputUtils.isRecipeViewOpen()) {
            AnvilRecipeStorage recipes = AnvilRecipeStorage.getInstance();
            int first = recipes.getFirstVisibleRecipeId();
            int countPerPage = recipes.getRecipeCountPerPage();
            int lastOnPage = first + countPerPage - 1;

            this.calculateRecipePositions(gui);

            ctx.pose().pushMatrix();
            ctx.pose().translate(this.recipeListX, this.recipeListY);
            ctx.pose().scale((float) this.scale, (float) this.scale);

            String str = StringUtils.translate("itemscroller.gui.label.recipe_page", (first / countPerPage) + 1, recipes.getTotalRecipeCount() / countPerPage);
            ctx.drawString(mc.font, str, 16, -12, 0xC0C0C0C0, false);

            for (int i = 0, recipeId = first; recipeId <= lastOnPage; i++, recipeId++) {
                ItemStack stack = recipes.getRecipe(recipeId).getResult();
                boolean selected = recipeId == recipes.getSelection();
                int row = i % this.recipesPerColumn;
                int column = i / this.recipesPerColumn;

                this.renderStoredRecipeStack(ctx, stack, recipeId, row, column, selected);
            }

            int hovered = this.getHoveredRecipeId(mouseX, mouseY, recipes, gui);
            AnvilRecipePattern recipe = hovered >= 0 ? recipes.getRecipe(hovered) : recipes.getRecipe(recipes.getSelection());
            this.renderRecipeItems(ctx, recipe);

            ctx.pose().popMatrix();
        }
    }

    private void calculateRecipePositions(AbstractContainerScreen<?> gui) {
        AnvilRecipeStorage recipes = AnvilRecipeStorage.getInstance();
        int gapHorizontal = 2;
        int gapVertical = 2;
        int stackBaseHeight = 16;

        this.recipesPerColumn = 9;
        this.columns = (int) Math.ceil((double) recipes.getRecipeCountPerPage() / (double) this.recipesPerColumn);
        this.numberTextWidth = 12;
        this.gapColumn = 4;

        int usableHeight = GuiUtils.getScaledWindowHeight();
        int usableWidth = ((AbstractContainerScreenAccessor) gui).crosspatch$getLeftPos();
        double gapScaleVertical = 1D - (double) gapVertical / (double) (stackBaseHeight + gapVertical);
        int maxStackDimensionsVertical = (int) ((usableHeight / ((double) this.recipesPerColumn + 1.2)) * gapScaleVertical);
        double gapScaleHorizontal = 1D - (double) gapHorizontal / (double) (stackBaseHeight + gapHorizontal);
        int maxStackDimensionsHorizontal = (int) (((usableWidth - (this.columns * (this.numberTextWidth + this.gapColumn))) / (this.columns + 3 + 0.8)) * gapScaleHorizontal);
        int stackDimensions = Math.min(maxStackDimensionsVertical, maxStackDimensionsHorizontal);

        this.scale = (double) stackDimensions / (double) stackBaseHeight;
        this.entryHeight = stackBaseHeight + gapVertical;
        this.recipeListX = usableWidth - (int) ((this.columns * (stackBaseHeight + this.numberTextWidth + this.gapColumn) + gapHorizontal) * this.scale);
        this.recipeListY = (int) (this.entryHeight * this.scale);
        this.columnWidth = stackBaseHeight + this.numberTextWidth + this.gapColumn;
    }

    private void renderHoverTooltip(GuiContext ctx, int mouseX, int mouseY, AnvilRecipePattern recipe) {
        ItemStack stack = recipe.getResult();

        if (!stack.isEmpty()) {
            InventoryOverlay.renderStackToolTip(ctx, mouseX, mouseY, stack);
        }
    }

    public int getHoveredRecipeId(int mouseX, int mouseY, AnvilRecipeStorage recipes, AbstractContainerScreen<?> gui) {
        if (AnvilInputUtils.isRecipeViewOpen()) {
            this.calculateRecipePositions(gui);
            int stackDimensions = (int) (16 * this.scale);

            for (int column = 0; column < this.columns; column++) {
                int startX = this.recipeListX + (int) ((column * this.columnWidth + this.gapColumn + this.numberTextWidth) * this.scale);

                if (mouseX >= startX && mouseX <= startX + stackDimensions) {
                    for (int row = 0; row < this.recipesPerColumn; row++) {
                        int startY = this.recipeListY + (int) (row * this.entryHeight * this.scale);

                        if (mouseY >= startY && mouseY <= startY + stackDimensions) {
                            return recipes.getFirstVisibleRecipeId() + column * this.recipesPerColumn + row;
                        }
                    }
                }
            }
        }

        return -1;
    }

    private void renderStoredRecipeStack(GuiContext ctx, ItemStack stack, int recipeId, int row, int column, boolean selected) {
        Font font = this.mc.font;
        String indexStr = String.valueOf(recipeId + 1);

        int x = column * this.columnWidth + this.gapColumn + this.numberTextWidth;
        int y = row * this.entryHeight;
        this.renderStackAt(ctx, stack, x, y, selected);

        float scale = 0.75F;
        x = x - (int) (font.width(indexStr) * scale) - 2;
        y = row * this.entryHeight + this.entryHeight / 2 - font.lineHeight / 2;

        ctx.pose().pushMatrix();
        ctx.pose().translate(x, y);
        ctx.pose().scale(scale, scale);
        ctx.drawString(font, indexStr, 0, 0, 0xFFC0C0C0, false);
        ctx.pose().popMatrix();
    }

    private void renderRecipeItems(GuiContext ctx, AnvilRecipePattern recipe) {
        // Preview the two input items stacked in the empty space left of the list.
        this.renderStackAt(ctx, recipe.getInput(), -3 * 17 + 2, 3 * this.entryHeight, false);
        this.renderStackAt(ctx, recipe.getAdditional(), -3 * 17 + 2, 3 * this.entryHeight + this.entryHeight, false);
    }

    private ItemStack getHoveredRecipeIngredient(int mouseX, int mouseY, AnvilRecipePattern recipe) {
        int scaledStackDimensions = (int) (16 * this.scale);
        int x = this.recipeListX - (int) ((3 * 17 - 2) * this.scale);
        int yInput = this.recipeListY + (int) (3 * this.entryHeight * this.scale);
        int yAdditional = this.recipeListY + (int) (4 * this.entryHeight * this.scale);

        if (mouseX >= x && mouseX <= x + scaledStackDimensions) {
            if (mouseY >= yInput && mouseY <= yInput + scaledStackDimensions) {
                return recipe.getInput();
            }

            if (mouseY >= yAdditional && mouseY <= yAdditional + scaledStackDimensions) {
                return recipe.getAdditional();
            }
        }

        return ItemStack.EMPTY;
    }

    private void renderStackAt(GuiContext ctx, ItemStack stack, int x, int y, boolean border) {
        int w = 16;

        if (border) {
            RenderUtils.drawOutline(ctx, x - 1, y - 1, w + 2, w + 2, 0xFFFFFFFF);
        }

        RenderUtils.drawRect(ctx, x, y, w, w, 0x20FFFFFF);

        if (!stack.isEmpty()) {
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            ctx.renderItem(renderStack, x, y);
        }
    }
}
