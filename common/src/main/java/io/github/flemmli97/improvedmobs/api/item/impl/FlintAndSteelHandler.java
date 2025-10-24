package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class FlintAndSteelHandler implements ItemUseHandler {

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        double dis = entity.position().distanceTo(target.position());
        if (dis < entity.getBbWidth() + target.getBbWidth() + 0.5 && !target.isOnFire()) {
            entity.playSound(SoundEvents.FLINTANDSTEEL_USE, 1, 1);
            target.igniteForSeconds(4);
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 30 + entity.getRandom().nextInt(30);
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.FLINT_AND_STEEL;
    }
}
