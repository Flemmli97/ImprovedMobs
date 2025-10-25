package io.github.flemmli97.improvedmobs.common.entities.ai.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AIUtils {

    //TODO building, stone, block;

    public static void setHeadingToPosition(ThrowableProjectile e, double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 dir = new Vec3(x - e.getX(), y - e.getY(), z - e.getZ()).scale(1 / velocity);
        e.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    public static void applyPotion(ThrownPotion entity, Iterable<MobEffectInstance> effects, @Nullable Entity p_213888_2_) {
        AABB axisalignedbb = entity.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = entity.level().getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        for (LivingEntity livingentity : list) {
            if (entity.getOwner() instanceof Mob && !livingentity.equals(((Mob) entity.getOwner()).getTarget()))
                continue;
            if (livingentity.isAffectedByPotions()) {
                double d0 = entity.distanceToSqr(livingentity);
                if (d0 < 16.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                    if (livingentity == p_213888_2_) {
                        d1 = 1.0D;
                    }

                    for (MobEffectInstance effectinstance : effects) {
                        MobEffect effect = effectinstance.getEffect().value();
                        if (effect.isInstantenous()) {
                            effect.applyInstantenousEffect(entity, entity.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                        } else {
                            int i = (int) (d1 * (double) effectinstance.getDuration() + 0.5D);
                            if (i > 20) {
                                livingentity.addEffect(new MobEffectInstance(effectinstance.getEffect(), i, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.isVisible()));
                            }
                        }
                    }
                }
            }
        }
    }
}
