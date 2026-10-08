package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.function.Predicate;

public final class PrimedStrikeTargeting {

    private PrimedStrikeTargeting() {
    }

    public static Entity findTarget(
            Player player,
            double range
    ) {
        if (player == null || player.level() == null) {
            return null;
        }

        if (range <= 0.0D) {
            return null;
        }

        Vec3 start =
                player.getEyePosition();

        Vec3 look =
                player.getLookAngle();

        Vec3 desiredEnd =
                start.add(
                        look.scale(range)
                );

        /*
         * First raycast against blocks.
         *
         * This prevents Primed Strike from attacking an entity
         * through a solid block.
         */
        BlockHitResult blockHit =
                player.level().clip(
                        new ClipContext(
                                start,
                                desiredEnd,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                player
                        )
                );

        Vec3 end =
                desiredEnd;

        if (blockHit.getType() != HitResult.Type.MISS) {
            end =
                    blockHit.getLocation();
        }

        /*
         * This matches vanilla's entity raycast structure.
         *
         * The final argument to ProjectileUtil.getEntityHitResult
         * is squared distance, not linear distance.
         */
        AABB searchBox =
                player.getBoundingBox()
                        .expandTowards(
                                look.scale(range)
                        )
                        .inflate(1.0D);

        Predicate<Entity> predicate =
                entity ->
                        entity != player
                                && !entity.isRemoved()
                                && entity.isPickable()
                                && entity.isAttackable()
                                && !entity.skipAttackInteraction(player);

        EntityHitResult hit =
                ProjectileUtil.getEntityHitResult(
                        player,
                        start,
                        end,
                        searchBox,
                        predicate,
                        range * range
                );

        if (hit == null) {
            return null;
        }

        Entity target =
                hit.getEntity();

        if (!canTarget(player, target)) {
            return null;
        }

        /*
         * Perform a final range check against the target's
         * actual bounding box. This prevents an entity whose
         * bounding box barely entered the search AABB from
         * being selected outside the intended interaction range.
         */
        if (!player.canInteractWithEntity(
                target.getBoundingBox(),
                range
        )) {
            return null;
        }

        return target;
    }

    private static boolean canTarget(
            Player player,
            Entity target
    ) {
        if (target == null) {
            return false;
        }

        if (target == player) {
            return false;
        }

        if (target.isRemoved()) {
            return false;
        }

        if (!target.isPickable()) {
            return false;
        }

        if (!target.isAttackable()) {
            return false;
        }

        return !target.skipAttackInteraction(player);
    }
}