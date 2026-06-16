package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.common.entities.ai.util.AIUtils;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class EnderpearlHandler implements ItemUseHandler {

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        double dis = entity.position().distanceToSqr(target.position());
        if (dis > 49.0) {
            Vec3 v1 = entity.position().subtract(target.position()).normalize().scale(16);
            double x = 0;
            double y = 0;
            double z = 0;
            if (entity.position().subtract(target.position()).length() > 16) {
                x = v1.x;
                y = v1.y;
                z = v1.z;
            }
            ThrownEnderpearl pearl = new ThrownEnderpearl(entity.level(), entity);
            EntityFlags.get(pearl).isThrownEntity = true;
            AIUtils.setHeadingToPosition(pearl, target.getX() - x, target.getY() - y, target.getZ() - z, 1.5F, 3.0F);
            entity.playSound(SoundEvents.ENDER_PEARL_THROW, 0.5f, 1);
            entity.level().addFreshEntity(pearl);
        }
        return 30 + entity.getRandom().nextInt(20);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.ENDER_PEARL;
    }
}
