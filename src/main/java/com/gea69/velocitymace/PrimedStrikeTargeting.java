package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class PrimedStrikeTargeting {

    private PrimedStrikeTargeting() {
    }

    public static Entity findTarget(
            Player player,
            double range
    ) {
        Level level = player.level();

        Vec3 start =
                player.getEyePosition();

        Vec3 look =
                player.getLookAngle();

        Vec3 desiredEnd =
                start.add(
                        look.scale(range)
                );

        /*
         * First determine how far the player can see before
         * hitting a solid block.
         */
        BlockHitResult blockHit =
                level.clip(
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

        if (blockHit.getType()
                != HitResult.Type.MISS) {

            end =
                    blockHit.getLocation();
        }

        double entityRange =
                start.distanceTo(end);

        if (entityRange <= 0.0D) {
            return null;
        }

        AABB searchBox =
                player.getBoundingBox()
                        .expandTowards(
                                look.scale(entityRange)
                        )
                        .inflate(1.0D);

        EntityHitResult entityHit =
                ProjectileUtil.getEntityHitResult(
                        player,
                        start,
                        end,
                        searchBox,
                        entity -> {
                            if (entity == player) {
                                return false;
                            }

                            if (!entity.isPickable()) {
                                return false;
                            }

                            if (!entity.isAttackable()) {
                                return false;
                            }

                            return !entity.skipAttackInteraction(
                                    player
                            );
                        },
                        entityRange
                );

        if (entityHit == null) {
            return null;
        }

        return entityHit.getEntity();
    }
}