package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class StealGoal extends MoveToBlockGoal {

    private final PathfinderMob entity;
    private int stealDelay;

    public StealGoal(PathfinderMob entity) {
        super(entity, 1, 9);
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        if (this.entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null)
            return false;
        return super.canUse() && this.entity.getTarget() == null;
    }

    @Override
    public void tick() {
        super.tick();
        this.stealDelay = Math.max(0, --this.stealDelay);
        BlockEntity tile = this.entity.level.getBlockEntity(this.blockPos);

        if (tile != null && this.stealDelay == 0 && this.entity.distanceToSqr(Vec3.atCenterOf(this.blockPos)) < 5 && this.canSee()) {
            ItemStack drop = CrossPlatformStuff.INSTANCE.lootRandomItem(tile, this.entity.getRandom());
            this.entity.level.playSound(null, this.entity.blockPosition(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.3F, 1);
            this.entity.swing(InteractionHand.MAIN_HAND);
            ItemEntity item = new ItemEntity(this.entity.level, this.entity.getX(), this.entity.getY(), this.entity.getZ(), drop);
            this.entity.level.addFreshEntity(item);
            this.stealDelay = 150 + this.entity.getRandom().nextInt(45);
        }
    }

    private boolean canSee() {
        Vec3 eyes = this.entity.getEyePosition(1);
        Vec3 block = Vec3.atCenterOf(this.blockPos);
        BlockHitResult res = this.entity.level.clip(new ClipContext(eyes, block, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.entity));
        return res.getType() == HitResult.Type.BLOCK && res.getBlockPos().equals(this.blockPos);
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null) {
            ResourceLocation res = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock());
            if (Config.CommonConfig.blackListedContainerBlocks.contains(res.toString()) || Config.CommonConfig.blackListedContainerBlocks.contains(res.getNamespace()))
                return false;
            return CrossPlatformStuff.INSTANCE.canLoot(tile);
        }
        return false;
    }
}
