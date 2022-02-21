package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.capability.ITileOpened;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Random;

public class StealGoal extends MoveToBlockGoal {

    private final CreatureEntity entity;
    private int stealDelay;

    public StealGoal(CreatureEntity entity) {
        super(entity, 1, 9);
        this.entity = entity;
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && this.entity.getAttackTarget() == null;
    }

    @Override
    public void tick() {
        super.tick();
        this.stealDelay = Math.max(0, --this.stealDelay);
        TileEntity tile = this.entity.world.getTileEntity(this.destinationBlock);

        if (tile != null && this.stealDelay == 0 && this.entity.getDistanceSq(Vector3d.copyCentered(this.destinationBlock)) < 5 && this.canSee()) {
            ItemStack drop = this.randomStack(tile, this.entity.getRNG());
            this.entity.world.playSound(null, this.entity.getPosition(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.3F, 1);
            this.entity.swingArm(Hand.MAIN_HAND);
            ItemEntity item = new ItemEntity(this.entity.world, this.entity.getPosX(), this.entity.getPosY(), this.entity.getPosZ(), drop);
            this.entity.world.addEntity(item);
            this.stealDelay = 150 + this.entity.getRNG().nextInt(45);
        }
    }

    private boolean canSee() {
        Vector3d eyes = this.entity.getEyePosition(1);
        Vector3d block = Vector3d.copyCentered(this.destinationBlock);
        BlockRayTraceResult res = this.entity.world.rayTraceBlocks(new RayTraceContext(eyes, block, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
        return res.getType() == RayTraceResult.Type.BLOCK && res.getPos().equals(this.destinationBlock);
    }

    private ItemStack randomStack(TileEntity blockEntity, Random rand) {
        return blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .map(cap -> {
                    ItemStack drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
                    int tries = 0;
                    while (drop.isEmpty() && tries < 10) {
                        drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
                        tries++;
                    }
                    return drop;
                }).orElse(ItemStack.EMPTY);
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            return tile.getCapability(TileCapProvider.OpenedCap).map(ITileOpened::playerOpened).orElse(false) &&
                    tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(cap -> {
                        for (int i = 0; i < cap.getSlots(); i++)
                            if (!cap.getStackInSlot(i).isEmpty())
                                return true;
                        return false;
                    }).orElse(false);
        }
        return false;
    }
}
