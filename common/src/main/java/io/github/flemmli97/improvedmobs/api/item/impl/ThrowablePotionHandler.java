package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.ImprovedMobsTags;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.StrafingMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.function.Function;

public class ThrowablePotionHandler implements ItemUseHandler {

    public static boolean isBadPotion(ItemStack stack) {
        for (MobEffectInstance effect : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects()) {
            if (effect.getEffect().is(ImprovedMobsTags.HARMFUL_EFFECT))
                return true;
        }
        return false;
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        double dis = entity.position().distanceTo(target.position());
        entity.playSound(SoundEvents.SPLASH_POTION_THROW, 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
        if (!entity.level().isClientSide) {
            ThrownPotion potion = new ThrownPotion(entity.level(), entity);
            potion.setItem(stack);
            potion.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), -30.0F, 0.2F + (float) (dis * 0.05), 1.2F);
            EntityFlags.get(potion).isThrownEntity = true;
            entity.level().addFreshEntity(potion);
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 80 + entity.getRandom().nextInt(10);
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof ThrowablePotionItem;
    }

    @Override
    public boolean canUse(LivingEntity entity, ItemStack stack, boolean attempt) {
        return isBadPotion(stack);
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return StrafingMover::new;
    }

    @Override
    public EquipmentSlot defaultedSlot() {
        return EquipmentSlot.OFFHAND;
    }
}
