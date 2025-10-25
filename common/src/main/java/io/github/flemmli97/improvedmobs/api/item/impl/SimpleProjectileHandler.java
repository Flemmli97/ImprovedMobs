package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.StrafingMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class SimpleProjectileHandler implements ItemUseHandler {

    private final Predicate<Item> matches;
    private final ToIntFunction<LivingEntity> cooldown;
    private final Function<LivingEntity, Projectile> factory;
    private final Supplier<SoundEvent> sound;

    public SimpleProjectileHandler(Predicate<Item> matches, ToIntFunction<LivingEntity> cooldown, Function<LivingEntity, Projectile> factory, Supplier<SoundEvent> sound) {
        this.matches = matches;
        this.cooldown = cooldown;
        this.factory = factory;
        this.sound = sound;
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.playSound(this.sound.get(), 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
        if (!entity.level().isClientSide) {
            Projectile projectile = this.factory.apply(entity);
            ;
            EntityFlags.get(projectile).isThrownEntity = true;
            entity.level().addFreshEntity(projectile);
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return this.cooldown.applyAsInt(entity);
    }

    @Override
    public boolean matches(Item item) {
        return this.matches.test(item);
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
