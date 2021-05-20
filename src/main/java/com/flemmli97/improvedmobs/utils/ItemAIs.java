package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ItemAIs {

    private final static String[] potionEffects = new String[]{"minecraft:regeneration", "minecraft:speed", "minecraft:strength", "minecraft:invisibility", "minecraft:resistance", "minecraft:fire_resistance"};

    public static ItemAI ENCHANTEDBOOK = new ItemAI() {
        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            if (!entity.world.isRemote) {
                List<Entity> nearby = entity.world.getEntitiesWithinAABBExcludingEntity(entity, entity.getBoundingBox().grow(8.0D));
                List<Entity> nearTarget = entity.world.getEntitiesWithinAABBExcludingEntity(entity.getAttackTarget(), entity.getAttackTarget().getBoundingBox().grow(2.0D));
                if (nearby.isEmpty() || nearby.size() == 1 && nearby.get(0) == entity.getAttackTarget() || entity.world.rand.nextInt(3) <= 1) {
                    if (nearTarget.isEmpty())
                        for (int x = -1; x <= 1; x++)
                            for (int z = -1; z <= 1; z++) {
                                if (x == 0 || z == 0) {
                                    Vector3d targetMotion = target.getMotion();
                                    EvokerFangsEntity fang = new EvokerFangsEntity(entity.world, target.getX() + x + targetMotion.x, target.getY(), target.getZ() + z + targetMotion.z, 0, 5, entity);
                                    entity.world.addEntity(fang);
                                }
                            }
                    else {
                        ShulkerBulletEntity bullet = new ShulkerBulletEntity(entity.world, entity, target, entity.getHorizontalFacing().getAxis());
                        bullet.getPersistentData().putBoolean(ImprovedMobs.thrownEntityID, true);
                        entity.world.addEntity(bullet);
                    }
                } else {
                    for (int i = 0; i < nearby.size(); i++) {
                        Entity entityRand = nearby.get(entity.world.rand.nextInt(nearby.size()));
                        if (entityRand instanceof MonsterEntity && entityRand != entity.getAttackTarget()) {
                            MonsterEntity mob = (MonsterEntity) entityRand;
                            mob.addPotionEffect(new EffectInstance(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionEffects[mob.world.rand.nextInt(6)])), 3600, 1));
                            entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.NEUTRAL, 2F, 1.0F);
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
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
        public int maxUseCount(MobEntity entity, Hand hand) {
            return 40;
        }
    };

    public static final ItemAI TNT = new ItemAI() {

        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            double dis = entity.getPositionVec().distanceTo(target.getPositionVec());
            if (!entity.world.isRemote) {
                TNTEntity tnt = new TNTEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), entity);
                ((ITNTThrowable) tnt).shootFromEntity(entity, entity.rotationPitch, entity.rotationYaw, -20.0F, 0.2F + (float) (dis * 0.05), 1.0F);
                tnt.getPersistentData().putBoolean(ImprovedMobs.thrownEntityID, true);
                entity.world.addEntity(tnt);
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            double dis = entity.getPositionVec().distanceTo(target.getPositionVec());
            if (dis < entity.getWidth() + target.getWidth() + 0.5 && !target.isBurning()) {
                target.setFire(4);
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            ItemStack stack = entity.getHeldItem(hand);
            if (AIUtils.isBadPotion(stack)) {
                double dis = entity.getPositionVec().distanceTo(target.getPositionVec());
                entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
                if (!entity.world.isRemote) {
                    PotionEntity potion = new PotionEntity(entity.world, entity);
                    potion.setItem(stack);
                    potion.setProperties(entity, entity.rotationPitch, entity.rotationYaw, -30.0F, 0.2F + (float) (dis * 0.05), 1.2F);
                    potion.getPersistentData().putBoolean(ImprovedMobs.thrownEntityID, true);
                    entity.world.addEntity(potion);
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
    };

    public static final ItemAI LINGERINGPOTIONS = new ItemAI() {

        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            ItemStack stack = entity.getHeldItem(hand);
            if (AIUtils.isBadPotion(stack)) {
                double dis = entity.getPositionVec().distanceTo(target.getPositionVec());
                entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
                if (!entity.world.isRemote) {
                    PotionEntity potion = new PotionEntity(entity.world, entity);
                    potion.setItem(stack);
                    potion.setProperties(entity, entity.rotationPitch, entity.rotationYaw, -30.0F, 0.2F + (float) (dis * 0.05), 1.2F);
                    potion.getPersistentData().putBoolean(ImprovedMobs.thrownEntityID, true);
                    entity.world.addEntity(potion);
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
    };

    public static final ItemAI CROSSBOWS = new ItemAI() {

        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            ItemStack stack = entity.getHeldItem(hand);
            float vel = CrossbowItem.hasChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
            CrossbowItem.fireProjectiles(entity.world, entity, hand, stack, vel, 1);
            CrossbowItem.setCharged(stack, false);
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
        public int maxUseCount(MobEntity entity, Hand hand) {
            return CrossbowItem.getChargeTime(entity.getHeldItem(hand)) + 5;
        }
    };

    public static final ItemAI BOWS = new ItemAI() {

        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            AIUtils.attackWithArrows(entity, target, BowItem.getArrowVelocity(entity.getItemInUseMaxCount()));
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
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
            return UsableHand.OFF;
        }

        @Override
        public boolean useHand() {
            return true;
        }

        @Override
        public int maxUseCount(MobEntity entity, Hand hand) {
            return 70;
        }
    };

    public static final ItemAI SNOWBALL = new ItemAI() {

        @Override
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
            if (!entity.world.isRemote) {
                SnowballEntity snowball = new SnowballEntity(entity.world, entity);
                snowball.setProperties(entity, entity.rotationPitch, entity.rotationYaw, 0, 1.5F, 1.0F);
                snowball.getPersistentData().putBoolean(ImprovedMobs.thrownEntityID, true);
                entity.world.addEntity(snowball);
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            double dis = entity.getPositionVec().squareDistanceTo(target.getPositionVec());
            if (dis > 49.0) {
                entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
                if (!entity.world.isRemote) {
                    Vector3d v1 = entity.getPositionVec().subtract(target.getPositionVec()).normalize().scale(16);
                    double x = 0;
                    double y = 0;
                    double z = 0;
                    if (entity.getPositionVec().subtract(target.getPositionVec()).length() > 16) {
                        x = v1.x;
                        y = v1.y;
                        z = v1.z;
                    }
                    EnderPearlEntity pearl = new EnderPearlEntity(entity.world, entity);
                    AIUtils.setHeadingToPosition(pearl, target.getX() - x, target.getY() - y, target.getZ() - z, 1.5F, 3.0F);
                    entity.world.addEntity(pearl);
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
        public void attack(MobEntity entity, LivingEntity target, Hand hand) {
            double dis = entity.getPositionVec().distanceTo(target.getPositionVec());
            if (dis < 8 && AIUtils.tryPlaceLava(entity.world, new BlockPos(target.getX() - 2 + entity.world.rand.nextInt(4), target.getY() - 1 + entity.world.rand.nextInt(2), target.getZ() - 2 + entity.world.rand.nextInt(4)))) {
                entity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 240, 1, true, false));
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