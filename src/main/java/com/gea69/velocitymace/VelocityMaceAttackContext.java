package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;

public final class VelocityMaceAttackContext {

    private static final ThreadLocal<Entity> ATTACK_TARGET = new ThreadLocal<>();

    private VelocityMaceAttackContext() {
    }

    public static void setTarget(Entity target) {
        ATTACK_TARGET.set(target);
    }

    public static Entity getTarget() {
        return ATTACK_TARGET.get();
    }

    public static void clearTarget() {
        ATTACK_TARGET.remove();
    }
}