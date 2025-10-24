package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LavaBucketHandler implements ItemUseHandler {

    public static boolean tryPlaceLava(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        boolean flag = !state.isSolid();
        boolean flag1 = state.canBeReplaced();

        if (!state.getFluidState().isEmpty())
            return false;
        if (!state.isAir() && !flag && !flag1)
            return false;
        if (!level.isClientSide && (flag || flag1) && !state.liquid()) {
            level.destroyBlock(pos, true);
        }
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 1), 11);
        return true;
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        double dis = entity.position().distanceToSqr(target.position());
        if (dis < 8 * 8 && tryPlaceLava(entity.level(), BlockPos.containing(target.getX() - 2 + entity.level().random.nextInt(4), target.getY() - 1 + entity.level().random.nextInt(2), target.getZ() - 2 + entity.level().random.nextInt(4)))) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 1, true, false));
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 80 + entity.getRandom().nextInt(20);
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.LAVA_BUCKET;
    }
}
