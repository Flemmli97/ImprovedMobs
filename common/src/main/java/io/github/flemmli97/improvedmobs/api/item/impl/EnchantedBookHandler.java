package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.ImprovedMobsTags;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.KeepDistanceMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

public class EnchantedBookHandler implements ItemUseHandler {

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        List<Entity> nearby = entity.level().getEntities(entity, entity.getBoundingBox().inflate(8.0D));
        List<Entity> nearTarget = entity.level().getEntities(target, target.getBoundingBox().inflate(2.0D));
        if (nearby.isEmpty() || nearby.size() == 1 && nearby.get(0) == target || entity.level().random.nextInt(3) <= 1) {
            if (nearTarget.isEmpty())
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 || z == 0) {
                            Vec3 targetMotion = target.getDeltaMovement();
                            EvokerFangs fang = new EvokerFangs(entity.level(), target.getX() + x + targetMotion.x, target.getY(), target.getZ() + z + targetMotion.z, 0, 5, entity);
                            entity.level().addFreshEntity(fang);
                        }
                    }
            else {
                ShulkerBullet bullet = new ShulkerBullet(entity.level(), entity, target, entity.getDirection().getAxis());
                EntityFlags.get(bullet).isThrownEntity = true;
                entity.level().addFreshEntity(bullet);
            }
        } else {
            for (int i = 0; i < nearby.size(); i++) {
                Entity entityRand = nearby.get(entity.level().random.nextInt(nearby.size()));
                if (entityRand instanceof Monster mob && entityRand != target) {
                    BuiltInRegistries.MOB_EFFECT.getTag(ImprovedMobsTags.ENCHANTED_BOOK_EFFECT)
                            .flatMap(n -> n.getRandomElement(mob.getRandom()))
                            .ifPresent(effect -> {
                                entity.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2, 1);
                                mob.addEffect(new MobEffectInstance(effect, 3600, 1));
                            });
                    return 80 + entity.getRandom().nextInt(30);
                }
            }
        }
        return 100;
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.ENCHANTED_BOOK;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return KeepDistanceMover::new;
    }
}
