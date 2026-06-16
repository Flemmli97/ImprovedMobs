package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.KeepDistanceMover;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;

import java.util.function.Function;

public class TridentHandler implements ItemUseHandler {

    @Override
    public void start(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.startUsingItem(hand);
    }

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.stopUsingItem();
        ItemStack trident = entity.getItemInHand(hand).getItem() instanceof TridentItem ? entity.getItemInHand(hand).copy() : new ItemStack(Items.TRIDENT);
        ThrownTrident tridententity = new ThrownTrident(entity.level(), entity, trident);
        double d0 = target.getX() - entity.getX();
        double d1 = target.getY(0.33) - tridententity.getY();
        double d2 = target.getZ() - entity.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        tridententity.shoot(d0, d1 + d3 * 0.2, d2, 1.6f, 14 - entity.level().getDifficulty().getId() * 4);
        entity.playSound(SoundEvents.DROWNED_SHOOT, 1, 1 / (entity.getRandom().nextFloat() * 0.4f + 0.8f));
        entity.level().addFreshEntity(tridententity);
        return 60 + entity.getRandom().nextInt(15);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return 40;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof TridentItem;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return KeepDistanceMover::new;
    }
}
