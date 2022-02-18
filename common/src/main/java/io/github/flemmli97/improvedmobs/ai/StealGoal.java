package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
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
        return super.canUse() && this.entity.getTarget() == null;
    }

    @Override
    public void tick() {
        super.tick();
        this.stealDelay = Math.max(0, --this.stealDelay);
        BlockEntity tile = this.entity.level.getBlockEntity(this.blockPos);

        if (tile instanceof Container inv && this.stealDelay == 0 && this.entity.distanceToSqr(Vec3.atCenterOf(this.blockPos)) < 5 && this.canSee()) {
            ItemStack drop = this.randomStack(inv);
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

    private ItemStack randomStack(Container inv) {
        try {
            if (!inv.isEmpty()) {
                ItemStack drop = inv.removeItem(this.entity.getRandom().nextInt(inv.getContainerSize()), 1);
                int tries = 0;
                while (drop.isEmpty() && tries < 10) {
                    drop = inv.removeItem(this.entity.getRandom().nextInt(inv.getContainerSize()), 1);
                    tries++;
                }
                return drop;
            }
            return ItemStack.EMPTY;
        } catch (Exception e) {
            ImprovedMobs.logger.error("#getSizeInventory and actual size of the inventory (" + inv + ") is not the same.");
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        boolean opened = false;
        if (tile instanceof Container) {
            opened = CrossPlatformStuff.instance().getTileData(tile).playerOpened();
        }
        return opened && !((Container) tile).isEmpty();
    }
}
