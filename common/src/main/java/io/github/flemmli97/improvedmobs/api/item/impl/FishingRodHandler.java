package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class FishingRodHandler implements ItemUseHandler {

    public static void ropeInTarget(LivingEntity entity, LivingEntity target) {
        Vec3 vec3 = entity.position().subtract(target.position()).normalize().scale(1.5);
        target.setDeltaMovement(target.getDeltaMovement().add(vec3.add(0, 0.3, 0)));
        target.hurtMarked = true;
        entity.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1, 0.4f / (entity.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        double dis = entity.position().distanceToSqr(target.position());
        if (dis < 100) {
            ropeInTarget(entity, target);
        }
        return 40 + entity.getRandom().nextInt(30);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof FishingRodItem;
    }
}
