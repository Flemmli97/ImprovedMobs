package io.github.flemmli97.improvedmobs.ai.util;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.mixinhelper.ITNTThrowable;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemAIs {

    private final static List<Holder<MobEffect>> POTION_EFFECTS = Lists.newArrayList(MobEffects.REGENERATION, MobEffects.MOVEMENT_SPEED,
            MobEffects.DAMAGE_BOOST, MobEffects.INVISIBILITY, MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE);

    public static final ItemAI ENCHANTEDBOOK = new ItemAI() {
        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            if (!entity.level().isClientSide) {
                List<Entity> nearby = entity.level().getEntities(entity, entity.getBoundingBox().inflate(8.0D));
                List<Entity> nearTarget = entity.level().getEntities(entity.getTarget(), entity.getTarget().getBoundingBox().inflate(2.0D));
                if (nearby.isEmpty() || nearby.size() == 1 && nearby.get(0) == entity.getTarget() || entity.level().random.nextInt(3) <= 1) {
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
                        if (entityRand instanceof Monster mob && entityRand != entity.getTarget()) {
                            mob.addEffect(new MobEffectInstance(POTION_EFFECTS.get(mob.level().random.nextInt(6)), 3600, 1));
                            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.NEUTRAL, 2F, 1.0F);
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public int cooldown() {
            return 80;
        }

        @Override
        public ItemType type() {
            return ItemType.STANDING;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };

    public static final ItemAI TRIDENT = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            AIUtils.tridentAttack(entity, target);
        }

        @Override
        public int cooldown() {
            return 65;
        }

        @Override
        public ItemType type() {
            return ItemType.STANDING;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }

        @Override
        public boolean useHand() {
            return true;
        }

        @Override
        public int maxUseCount(Mob entity, InteractionHand hand) {
            return 40;
        }
    };

    public static final ItemAI TNT = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            double dis = entity.position().distanceTo(target.position());
            if (!entity.level().isClientSide) {
                PrimedTnt tnt = new PrimedTnt(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
                ((ITNTThrowable) tnt).shootFromEntity(entity, entity.getXRot(), entity.getYRot(), -20.0F, 0.2F + (float) (dis * 0.05), 1.0F);
                EntityFlags.get(tnt).isThrownEntity = true;
                entity.level().addFreshEntity(tnt);
            }
        }

        @Override
        public int cooldown() {
            return 65;
        }

        @Override
        public ItemType type() {
            return ItemType.STRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };

    public static final ItemAI FLINT_N_STEEL = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            double dis = entity.position().distanceTo(target.position());
            if (dis < entity.getBbWidth() + target.getBbWidth() + 0.5 && !target.isOnFire()) {
                target.setRemainingFireTicks(4);
            }
        }

        @Override
        public int cooldown() {
            return 25;
        }

        @Override
        public ItemType type() {
            return ItemType.NONSTRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };

    public static final ItemAI SPLASH = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            ItemStack stack = entity.getItemInHand(hand);
            if (AIUtils.isBadPotion(stack)) {
                double dis = entity.position().distanceTo(target.position());
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
                if (!entity.level().isClientSide) {
                    ThrownPotion potion = new ThrownPotion(entity.level(), entity);
                    potion.setItem(stack);
                    potion.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), -30.0F, 0.2F + (float) (dis * 0.05), 1.2F);
                    EntityFlags.get(potion).isThrownEntity = true;
                    entity.level().addFreshEntity(potion);
                }
            }
        }

        @Override
        public int cooldown() {
            return 85;
        }

        @Override
        public ItemType type() {
            return ItemType.STRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }

        @Override
        public boolean applies(ItemStack stack) {
            return AIUtils.isBadPotion(stack);
        }
    };

    public static final ItemAI LINGERINGPOTIONS = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            ItemStack stack = entity.getItemInHand(hand);
            if (AIUtils.isBadPotion(stack)) {
                double dis = entity.position().distanceTo(target.position());
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
                if (!entity.level().isClientSide) {
                    ThrownPotion potion = new ThrownPotion(entity.level(), entity);
                    potion.setItem(stack);
                    potion.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), -30.0F, 0.2F + (float) (dis * 0.05), 1.2F);
                    EntityFlags.get(potion).isThrownEntity = true;
                    entity.level().addFreshEntity(potion);
                }
            }
        }

        @Override
        public int cooldown() {
            return 85;
        }

        @Override
        public ItemType type() {
            return ItemType.STRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }

        @Override
        public boolean applies(ItemStack stack) {
            return AIUtils.isBadPotion(stack);
        }
    };

    public static final ItemAI CROSSBOWS = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            ItemStack stack = entity.getItemInHand(hand);
            if (stack.getItem() instanceof CrossbowItem crossbow) {
                ChargedProjectiles projectile = stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
                float vel = projectile != null && projectile.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
                crossbow.performShooting(entity.level(), entity, hand, stack, vel, 13.5f - entity.level().getDifficulty().getId() * 4, target);
            }
        }

        @Override
        public int cooldown() {
            return 20;
        }

        @Override
        public ItemType type() {
            return ItemType.STANDING;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }

        @Override
        public boolean useHand() {
            return true;
        }

        @Override
        public int maxUseCount(Mob entity, InteractionHand hand) {
            return CrossbowItem.getChargeDuration(entity.getItemInHand(hand)) + 5;
        }
    };

    public static final ItemAI BOWS = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            AIUtils.attackWithArrows(entity, target, BowItem.getPowerForTime(entity.getTicksUsingItem()));
        }

        @Override
        public int cooldown() {
            return 30;
        }

        @Override
        public ItemType type() {
            return ItemType.STRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }

        @Override
        public boolean useHand() {
            return true;
        }
    };

    public static final ItemAI SHIELDS = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
        }

        @Override
        public int cooldown() {
            return 60;
        }

        @Override
        public ItemType type() {
            return ItemType.NONSTRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.OFF;
        }

        @Override
        public boolean useHand() {
            return true;
        }

        @Override
        public int maxUseCount(Mob entity, InteractionHand hand) {
            return 75;
        }

        @Override
        public boolean isIncompatibleWith(LivingEntity entity, ItemStack stack) {
            return stack.getItem() instanceof CrossbowItem || (entity instanceof AbstractSkeleton && stack.getItem() instanceof BowItem);
        }
    };

    public static final ItemAI SNOWBALL = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
            if (!entity.level().isClientSide) {
                Snowball snowball = new Snowball(entity.level(), entity);
                snowball.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0, 1.5F, 1.0F);
                EntityFlags.get(snowball).isThrownEntity = true;
                entity.level().addFreshEntity(snowball);
            }
        }

        @Override
        public int cooldown() {
            return 25;
        }

        @Override
        public ItemType type() {
            return ItemType.STRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };

    public static final ItemAI ENDER_PEARL = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            double dis = entity.position().distanceToSqr(target.position());
            if (dis > 49.0) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.level().random.nextFloat() * 0.4F + 0.8F));
                if (!entity.level().isClientSide) {
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
                    AIUtils.setHeadingToPosition(pearl, target.getX() - x, target.getY() - y, target.getZ() - z, 1.5F, 3.0F);
                    entity.level().addFreshEntity(pearl);
                }
            }
        }

        @Override
        public int cooldown() {
            return 35;
        }

        @Override
        public ItemType type() {
            return ItemType.NONSTRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };

    public static final ItemAI LAVABUCKET = new ItemAI() {

        @Override
        public void attack(Mob entity, LivingEntity target, InteractionHand hand) {
            double dis = entity.position().distanceTo(target.position());
            if (dis < 8 && AIUtils.tryPlaceLava(entity.level(), BlockPos.containing(target.getX() - 2 + entity.level().random.nextInt(4), target.getY() - 1 + entity.level().random.nextInt(2), target.getZ() - 2 + entity.level().random.nextInt(4)))) {
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 1, true, false));
            }
        }

        @Override
        public int cooldown() {
            return 80;
        }

        @Override
        public ItemType type() {
            return ItemType.NONSTRAFINGITEM;
        }

        @Override
        public UsableHand prefHand() {
            return UsableHand.BOTH;
        }
    };
}