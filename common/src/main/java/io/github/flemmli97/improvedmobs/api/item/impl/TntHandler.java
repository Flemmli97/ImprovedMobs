package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.StrafingMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import io.github.flemmli97.improvedmobs.mixinhelper.TNTExtension;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Function;

public class TntHandler implements ItemUseHandler {

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        double dis = entity.position().distanceTo(target.position());
        if (!entity.level().isClientSide) {
            PrimedTnt tnt = new PrimedTnt(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
            ((TNTExtension) tnt).improvedMobs$shootFromEntity(entity, entity.getXRot(), entity.getYRot(), -20.0F, 0.2F + (float) (dis * 0.05), 1.0F);
            EntityFlags.get(tnt).isThrownEntity = true;
            entity.level().addFreshEntity(tnt);
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 60 + entity.getRandom().nextInt(20);
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return 0;
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.TNT;
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
