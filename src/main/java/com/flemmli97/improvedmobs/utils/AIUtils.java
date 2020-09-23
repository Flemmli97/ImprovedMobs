package com.flemmli97.improvedmobs.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class AIUtils {

    //TODO building, stone, block;
    //TODO fishing rod

    public static void setHeadingToPosition(ThrowableEntity e, double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d dir = new Vector3d(x - e.getX(), y - e.getY(), z - e.getZ()).scale(1 / velocity);
        e.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    public static void attackWithArrows(ArrowEntity arrow, LivingEntity theEntity, LivingEntity target, float distanceFactor) {
        double d0 = target.getX() - theEntity.getX();
        double d1 = target.getBoundingBox(target.getPose()).minY + (double) (target.getHeight() / 3.0F) - arrow.getY();
        if (target.getHeight() < 0.5)
            d1 = target.getBoundingBox(target.getPose()).minY - arrow.getY();
        double d2 = target.getZ() - theEntity.getZ();
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float) (14 - theEntity.world.getDifficulty().getId() * 4));
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, theEntity);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, theEntity);
        arrow.setDamage((double) (distanceFactor * 2.0F) + theEntity.world.rand.nextGaussian() * 0.25D + (double) ((float) theEntity.world.getDifficulty().getId() * 0.11F));

        if (i > 0) {
            arrow.setDamage(arrow.getDamage() + (double) i * 0.5D + 0.5D);
        }

        if (j > 0) {
            arrow.setKnockbackStrength(j);
        }

        boolean flag = theEntity.isBurning() && theEntity.world.getDifficulty() == Difficulty.HARD && theEntity.world.rand.nextBoolean();
        flag = flag || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, theEntity) > 0;

        if (flag) {
            arrow.setFire(100);
        }

        ItemStack itemstack = theEntity.getHeldItem(Hand.OFF_HAND);
        if (itemstack.getItem() == Items.TIPPED_ARROW) {
            arrow.setPotionEffect(itemstack);
        }
        theEntity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (theEntity.getRNG().nextFloat() * 0.4F + 0.8F));
        theEntity.world.addEntity(arrow);
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
}
