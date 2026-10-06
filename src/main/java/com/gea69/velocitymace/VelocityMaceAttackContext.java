package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class VelocityMaceAttackContext {

    private static final ThreadLocal<Player> ATTACKER =
            new ThreadLocal<>();

    private static final ThreadLocal<Entity> ATTACK_TARGET =
            new ThreadLocal<>();

    private static final ThreadLocal<Vec3> ATTACKER_VELOCITY =
            new ThreadLocal<>();

    private static final ThreadLocal<Vec3> TARGET_VELOCITY =
            new ThreadLocal<>();

    private static final ThreadLocal<ItemStack> WEAPON =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> FALL_FLYING =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> FLYING =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> SPRINTING =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> SWIMMING =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> CRAWLING =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> RIDING =
            new ThreadLocal<>();

    private VelocityMaceAttackContext() {
    }

    public static void setAttack(
            Player attacker,
            Entity target,
            Vec3 attackerVelocity,
            Vec3 targetVelocity
    ) {
        ATTACKER.set(attacker);
        ATTACK_TARGET.set(target);
        ATTACKER_VELOCITY.set(attackerVelocity);
        TARGET_VELOCITY.set(targetVelocity);

        /*
         * Snapshot the movement state at the exact beginning
         * of Player.attack().
         */
        FALL_FLYING.set(
                attacker.isFallFlying()
        );

        boolean flying = false;

        try {
            flying = attacker.getAbilities().flying;
        } catch (Exception ignored) {
            flying = false;
        }

        FLYING.set(flying);

        SPRINTING.set(
                attacker.isSprinting()
        );

        SWIMMING.set(
                attacker.isSwimming()
        );

        CRAWLING.set(
                attacker.isVisuallyCrawling()
        );

        RIDING.set(
                attacker.isPassenger()
        );
    }

    public static Player getAttacker() {
        return ATTACKER.get();
    }

    public static Entity getTarget() {
        return ATTACK_TARGET.get();
    }

    public static Vec3 getAttackerVelocity() {
        return ATTACKER_VELOCITY.get();
    }

    public static Vec3 getTargetVelocity() {
        return TARGET_VELOCITY.get();
    }

    public static ItemStack getWeapon() {
        return WEAPON.get();
    }

    public static void setWeapon(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            WEAPON.set(ItemStack.EMPTY);
        } else {
            WEAPON.set(weapon.copy());
        }
    }

    public static boolean isFallFlying() {
        Boolean value = FALL_FLYING.get();
        return value != null && value;
    }

    public static boolean isFlying() {
        Boolean value = FLYING.get();
        return value != null && value;
    }

    public static boolean isSprinting() {
        Boolean value = SPRINTING.get();
        return value != null && value;
    }

    public static boolean isSwimming() {
        Boolean value = SWIMMING.get();
        return value != null && value;
    }

    public static boolean isCrawling() {
        Boolean value = CRAWLING.get();
        return value != null && value;
    }

    public static boolean isRiding() {
        Boolean value = RIDING.get();
        return value != null && value;
    }

    public static void clearTarget() {
        ATTACKER.remove();
        ATTACK_TARGET.remove();
        ATTACKER_VELOCITY.remove();
        TARGET_VELOCITY.remove();
        WEAPON.remove();

        FALL_FLYING.remove();
        FLYING.remove();
        SPRINTING.remove();
        SWIMMING.remove();
        CRAWLING.remove();
        RIDING.remove();
    }
}