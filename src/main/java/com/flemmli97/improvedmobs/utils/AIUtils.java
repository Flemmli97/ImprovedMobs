package com.flemmli97.improvedmobs.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class AIUtils {

    //TODO building, stone, block;
    //TODO fishing rod

    public static void setHeadingToPosition(ThrowableEntity e, double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d dir = new Vector3d(x - e.getPosX(), y - e.getPosY(), z - e.getPosZ()).scale(1 / velocity);
        e.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    public static void attackWithArrows(MobEntity entity, LivingEntity target, float distanceFactor) {
        ItemStack itemstack = entity.findAmmo(entity.getHeldItem(ProjectileHelper.getHandWith(entity, Items.BOW)));
        AbstractArrowEntity abstractarrowentity = ProjectileHelper.fireArrow(entity, itemstack, distanceFactor);
        if (entity.getHeldItemMainhand().getItem() instanceof net.minecraft.item.BowItem)
            abstractarrowentity = ((net.minecraft.item.BowItem) entity.getHeldItemMainhand().getItem()).customArrow(abstractarrowentity);
        double d0 = target.getPosX() - entity.getPosX();
        double d1 = target.getPosYHeight(0.3333333333333333D) - abstractarrowentity.getPosY();
        double d2 = target.getPosZ() - entity.getPosZ();
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - entity.world.getDifficulty().getId() * 4));
        entity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRNG().nextFloat() * 0.4F + 0.8F));
        entity.world.addEntity(abstractarrowentity);
    }

    public static void tridentAttack(MobEntity entity, LivingEntity target) {
        TridentEntity tridententity = new TridentEntity(entity.world, entity, new ItemStack(Items.TRIDENT));
        double d0 = target.getPosX() - entity.getPosX();
        double d1 = target.getPosYHeight(0.3333333333333333D) - tridententity.getPosY();
        double d2 = target.getPosZ() - entity.getPosZ();
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        tridententity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - entity.world.getDifficulty().getId() * 4));
        entity.playSound(SoundEvents.ENTITY_DROWNED_SHOOT, 1.0F, 1.0F / (entity.getRNG().nextFloat() * 0.4F + 0.8F));
        entity.world.addEntity(tridententity);
    }

    public static boolean tryPlaceLava(World worldIn, BlockPos posIn) {
        BlockState iblockstate = worldIn.getBlockState(posIn);
        Material material = iblockstate.getMaterial();
        boolean flag = !material.isSolid();
        boolean flag1 = iblockstate.getMaterial().isReplaceable();

        if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
            return false;
        } else {
            if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid()) {
                worldIn.destroyBlock(posIn, true);
            }
            worldIn.playSound(null, posIn, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
            worldIn.setBlockState(posIn, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 1), 11);
            return true;
        }
    }

    public static boolean isBadPotion(ItemStack stack) {
        for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack)) {
            if (effect.getPotion().getEffectType() == EffectType.HARMFUL)
                return true;
        }
        return false;
    }

    public static void applyPotion(PotionEntity entity, List<EffectInstance> p_213888_1_, @Nullable Entity p_213888_2_) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = entity.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);
        for (LivingEntity livingentity : list) {
            if (entity.getShooter() instanceof MobEntity && !livingentity.equals(((MobEntity) entity.getShooter()).getAttackTarget()))
                continue;
            if (livingentity.canBeHitWithPotion()) {
                double d0 = entity.getDistanceSq(livingentity);
                if (d0 < 16.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                    if (livingentity == p_213888_2_) {
                        d1 = 1.0D;
                    }

                    for (EffectInstance effectinstance : p_213888_1_) {
                        Effect effect = effectinstance.getPotion();
                        if (effect.isInstant()) {
                            effect.affectEntity(entity, entity.getShooter(), livingentity, effectinstance.getAmplifier(), d1);
                        } else {
                            int i = (int) (d1 * (double) effectinstance.getDuration() + 0.5D);
                            if (i > 20) {
                                livingentity.addPotionEffect(new EffectInstance(effect, i, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.doesShowParticles()));
                            }
                        }
                    }
                }
            }
        }
    }
}
