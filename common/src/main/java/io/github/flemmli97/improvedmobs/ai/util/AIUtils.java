package io.github.flemmli97.improvedmobs.ai.util;

import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AIUtils {

    //TODO building, stone, block;
    //TODO fishing rod

    public static void setHeadingToPosition(ThrowableProjectile e, double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 dir = new Vec3(x - e.getX(), y - e.getY(), z - e.getZ()).scale(1 / velocity);
        e.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    public static void attackWithArrows(Mob entity, LivingEntity target, float distanceFactor) {
        ItemStack itemstack = entity.getProjectile(entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, Items.BOW)));
        AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(entity, itemstack, distanceFactor);
        if (entity.getMainHandItem().getItem() instanceof BowItem)
            abstractarrowentity = CrossPlatformStuff.INSTANCE.customBowArrow((BowItem) entity.getMainHandItem().getItem(), abstractarrowentity);
        double d0 = target.getX() - entity.getX();
        double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
        double d2 = target.getZ() - entity.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - entity.level.getDifficulty().getId() * 4));
        entity.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        entity.level.addFreshEntity(abstractarrowentity);
    }

    public static void tridentAttack(Mob entity, LivingEntity target) {
        ThrownTrident tridententity = new ThrownTrident(entity.level, entity, new ItemStack(Items.TRIDENT));
        double d0 = target.getX() - entity.getX();
        double d1 = target.getY(0.3333333333333333D) - tridententity.getY();
        double d2 = target.getZ() - entity.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        tridententity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - entity.level.getDifficulty().getId() * 4));
        entity.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        entity.level.addFreshEntity(tridententity);
    }

    public static boolean tryPlaceLava(Level worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        Material material = state.getMaterial();
        boolean flag = !material.isSolid();
        boolean flag1 = state.getMaterial().isReplaceable();

        if (!state.getFluidState().isEmpty())
            return false;
        if (!state.isAir() && !flag && !flag1)
            return false;
        if (!worldIn.isClientSide && (flag || flag1) && !material.isLiquid()) {
            worldIn.destroyBlock(pos, true);
        }
        worldIn.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
        worldIn.setBlock(pos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 1), 11);
        return true;
    }

    public static boolean isBadPotion(ItemStack stack) {
        for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                return true;
        }
        return false;
    }

    public static void applyPotion(ThrownPotion entity, List<MobEffectInstance> p_213888_1_, @Nullable Entity p_213888_2_) {
        AABB axisalignedbb = entity.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = entity.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        for (LivingEntity livingentity : list) {
            if (entity.getOwner() instanceof Mob && !livingentity.equals(((Mob) entity.getOwner()).getTarget()))
                continue;
            if (livingentity.isAffectedByPotions()) {
                double d0 = entity.distanceToSqr(livingentity);
                if (d0 < 16.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                    if (livingentity == p_213888_2_) {
                        d1 = 1.0D;
                    }

                    for (MobEffectInstance effectinstance : p_213888_1_) {
                        MobEffect effect = effectinstance.getEffect();
                        if (effect.isInstantenous()) {
                            effect.applyInstantenousEffect(entity, entity.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                        } else {
                            int i = (int) (d1 * (double) effectinstance.getDuration() + 0.5D);
                            if (i > 20) {
                                livingentity.addEffect(new MobEffectInstance(effect, i, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.isVisible()));
                            }
                        }
                    }
                }
            }
        }
    }
}
