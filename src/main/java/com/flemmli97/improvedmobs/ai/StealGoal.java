package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.capability.ITileOpened;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
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

import java.util.Optional;

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

        if (tile instanceof IInventory && this.stealDelay == 0 && this.entity.getDistanceSq(Vector3d.copyCentered(this.destinationBlock)) < 5 && this.canSee()) {
            IInventory inv = (IInventory) tile;
            ItemStack drop = this.randomStack(inv);
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

    private ItemStack randomStack(IInventory inv) {
        try {
            if (!inv.isEmpty()) {
                ItemStack drop = inv.decrStackSize(this.entity.getRNG().nextInt(inv.getSizeInventory()), 1);
                int tries = 0;
                while (drop.isEmpty() && tries < 10) {
                    drop = inv.decrStackSize(this.entity.getRNG().nextInt(inv.getSizeInventory()), 1);
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
    protected boolean shouldMoveTo(IWorldReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        boolean opened = false;
        if (tile instanceof IInventory) {
            Optional<ITileOpened> cap = tile.getCapability(TileCapProvider.OpenedCap, null).resolve();
            if (cap.isPresent())
                opened = cap.get().playerOpened();
        }
        return opened && !((IInventory) tile).isEmpty();
    }
}
