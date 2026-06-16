package com.rs256.crossPatch.client.pickblock;

import com.mojang.authlib.GameProfile;
import com.rs256.crossPatch.client.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Core logic for the {@code pickBlockPro} feature.
 *
 * <p>Modern pick block is server-authoritative and reach-limited, so this handler instead
 * ray-traces against the {@link ClientLevel} and resolves the pick entirely client-side,
 * handing the resulting item to {@link PickBlockInventory} (which places it with only
 * reach-free packets). Two sub-features feed into it:</p>
 * <ul>
 *     <li>{@link Configs.PickBlock#PICK_BLOCK_PRO} — extends pick reach for blocks (and,
 *     together with the head feature, for players) out to
 *     {@link Configs.PickBlock#PICK_BLOCK_PRO_REACH_OVERRIDE}.</li>
 *     <li>{@link Configs.PickBlock#PICK_BLOCK_PRO_PICK_PLAYER_HEAD} — picking a player yields
 *     that player's current head.</li>
 * </ul>
 *
 * <p>It is non-destructive: blocks within normal reach (and non-player entities) are left to
 * vanilla, preserving its existing behaviour.</p>
 */
public final class PickBlockProHandler {
    private PickBlockProHandler() {
    }

    /**
     * Attempts a client-side pick (extended block reach and/or player head).
     *
     * @param mc the client instance
     * @return {@code true} if this handler performed the pick and the vanilla pick block
     * logic should be cancelled; {@code false} to let vanilla run unchanged.
     */
    public static boolean tryHandlePick(Minecraft mc) {
        boolean reachExtension = Configs.PickBlock.PICK_BLOCK_PRO.getBooleanValue();
        boolean pickPlayerHead = Configs.PickBlock.PICK_BLOCK_PRO_PICK_PLAYER_HEAD.getBooleanValue();
        boolean pickShulker = Configs.PickBlock.PICK_BLOCK_PRO_PICK_SHULKER_WITH_ITEM.getBooleanValue();
        boolean hotbarLock = HotbarSlotLock.isRestricted();
        boolean pickRedirect = PickRedirect.isEnabled();
        if (!reachExtension && !pickPlayerHead && !pickShulker && !hotbarLock && !pickRedirect) {
            return false;
        }

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || mc.gameMode == null || level == null) {
            return false;
        }

        double override = Configs.PickBlock.PICK_BLOCK_PRO_REACH_OVERRIDE.getDoubleValue();
        double blockReach = player.blockInteractionRange();
        double entityReach = player.entityInteractionRange();
        double blockRange = reachExtension ? Math.max(override, blockReach) : blockReach;
        // Players are only picked when the head feature is on; extend that reach only when
        // the reach extension is also enabled.
        double entityRange = reachExtension ? Math.max(override, entityReach) : entityReach;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        HitResult hit = raycast(player, level, blockRange, entityRange, pickPlayerHead, partialTick);

        // Player head: override the pick whenever a player is the targeted (nearest) thing.
        if (pickPlayerHead
                && hit.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult) hit).getEntity() instanceof Player target) {
            return PickBlockInventory.pickOrPlace(mc, createPlayerHead(target));
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            Vec3 eye = player.getEyePosition(partialTick);
            boolean beyondReach = eye.distanceToSqr(blockHit.getLocation()) > blockReach * blockReach;

            // Blocks out of normal reach: take over the pick entirely (needs reach extension).
            if (beyondReach) {
                if (!reachExtension) {
                    return false;
                }
                ItemStack item = resolveBlockItem(mc, player, level, blockHit);
                return !item.isEmpty() && PickBlockInventory.pickOrPlace(mc, item);
            }

            // Within normal reach we normally defer to vanilla. We only step in when a sub-feature
            // changes the outcome:
            //  - the hotbar lock needs every pick routed through our placement (any mode);
            //  - a redirect changes which item the block yields;
            //  - the survival shulker redirect needs to substitute the shulker box.
            boolean redirected = pickRedirect
                    && PickRedirect.lookup(level.getBlockState(blockHit.getBlockPos()).getBlock()) != null;
            if (hotbarLock || redirected) {
                // Take over the whole pick (resolveBlockItem applies any redirect).
                ItemStack item = resolveBlockItem(mc, player, level, blockHit);
                return !item.isEmpty() && PickBlockInventory.pickOrPlace(mc, item);
            }

            if (pickShulker && !player.hasInfiniteMaterials()) {
                ItemStack item = resolveBlockItem(mc, player, level, blockHit);
                if (!item.isEmpty()
                        && player.getInventory().findSlotMatchingItem(item) == Inventory.NOT_FOUND_INDEX) {
                    return PickBlockInventory.bringContainingShulker(mc, item);
                }
            }
        }

        return false;
    }

    /**
     * Ray-traces from the player's eyes, returning the nearest of a block hit (up to
     * {@code blockRange}) and, when {@code includeEntities} is set, an entity hit (up to
     * {@code entityRange}). Mirrors vanilla's crosshair pick but with configurable ranges.
     */
    private static HitResult raycast(
            LocalPlayer player,
            ClientLevel level,
            double blockRange,
            double entityRange,
            boolean includeEntities,
            float partialTick
    ) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 view = player.getViewVector(partialTick);
        Vec3 blockEnd = eye.add(view.scale(blockRange));
        BlockHitResult blockHit = level.clip(new ClipContext(
                eye, blockEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (!includeEntities) {
            return blockHit;
        }

        Vec3 entityEnd = eye.add(view.scale(entityRange));
        AABB searchBox = player.getBoundingBox().expandTowards(view.scale(entityRange)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, eye, entityEnd, searchBox,
                target -> !target.isSpectator() && target.isPickable(),
                entityRange * entityRange);

        if (entityHit == null) {
            return blockHit;
        }

        // Prefer whichever hit is closer to the eyes.
        if (blockHit.getType() == HitResult.Type.BLOCK
                && eye.distanceToSqr(blockHit.getLocation()) < eye.distanceToSqr(entityHit.getLocation())) {
            return blockHit;
        }

        return entityHit;
    }

    /**
     * Builds a player head carrying {@code target}'s current resolved profile (skin).
     *
     * <p>If the player's profile cannot be resolved into a head — i.e. it is not a fully
     * formed profile (missing UUID or name) — a plain, owner-less player head is returned
     * instead of attaching a malformed profile.</p>
     */
    private static ItemStack createPlayerHead(Player target) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = target.getGameProfile();
        if (profile != null && isResolvableProfile(profile)) {
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        }
        return head;
    }

    /**
     * @return {@code true} when {@code profile} is complete enough to build a meaningful head
     * (has both a UUID and a name). Equivalent to authlib's old {@code GameProfile#isComplete},
     * which no longer exists on the record-based authlib both target versions use.
     */
    private static boolean isResolvableProfile(GameProfile profile) {
        return profile.id() != null && profile.name() != null && !profile.name().isEmpty();
    }

    /**
     * Resolves the block under {@code blockHit} into its pick-block item using the client world.
     * Mirrors vanilla: block-entity NBT is only folded in for a creative Ctrl-pick.
     */
    private static ItemStack resolveBlockItem(
            Minecraft mc,
            LocalPlayer player,
            ClientLevel level,
            BlockHitResult blockHit
    ) {
        BlockState state = level.getBlockState(blockHit.getBlockPos());
        if (state.isAir()) {
            return ItemStack.EMPTY;
        }

        // Redirect the picked block to a configured replacement item, if any.
        Item redirect = PickRedirect.lookup(state.getBlock());
        if (redirect != null) {
            return new ItemStack(redirect);
        }

        boolean includeData = player.hasInfiniteMaterials() && mc.hasControlDown();
        return state.getCloneItemStack(level, blockHit.getBlockPos(), includeData);
    }
}
