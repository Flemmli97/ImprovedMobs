package com.flemmli97.improvedmobs.entity.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapProvider;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAISteal extends EntityAIMoveToBlock{

	EntityLiving living;	
	private int stealDelay;
	public EntityAISteal(EntityMob living)
	{
		super(living, 1, 8);
		this.living=living;
	}
	@Override
	public boolean shouldExecute() {
		return super.shouldExecute() && this.living.getAttackTarget()==null;
	}
	
	@Override
	public void updateTask()
    {
		super.updateTask();
		this.stealDelay=Math.max(0, --this.stealDelay);
		TileEntity tile = this.living.world.getTileEntity(this.destinationBlock);
		
		if(tile instanceof IInventory && this.stealDelay==0 && this.living.getDistanceSq(this.destinationBlock)<5 && this.canSee())
		{
			IInventory inv = (IInventory) tile;
			ItemStack drop = this.randomStack(inv);
			this.living.world.playSound(null, this.living.getPosition(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.3F, 1);
			this.living.swingArm(EnumHand.MAIN_HAND);
			EntityItem item = new EntityItem(this.living.world, this.living.posX, this.living.posY, this.living.posZ, drop);
			this.living.world.spawnEntity(item);
			this.stealDelay=150+this.living.getRNG().nextInt(45);
		}
    }
	
	private boolean canSee()
	{
		Vec3d eyes = this.living.getPositionEyes(1);
		Vec3d block = new Vec3d(this.destinationBlock);
		RayTraceResult res = this.living.world.rayTraceBlocks(eyes, block);
		return res!=null && res.getBlockPos().equals(this.destinationBlock);
	}
	
	private ItemStack randomStack(IInventory inv)
	{
		try
		{
			if(!inv.isEmpty())
			{
				ItemStack drop = inv.decrStackSize(this.living.getRNG().nextInt(inv.getSizeInventory()), 1);
				while(drop.isEmpty())
					drop = inv.decrStackSize(this.living.getRNG().nextInt(inv.getSizeInventory()), 1);
				return drop;
			}
			return ItemStack.EMPTY;
		}
		catch(Exception e)
		{
			ImprovedMobs.logger.error("#getSizeInventory and actual size of the inventory ("+inv +  ") is not the same.");
			return ItemStack.EMPTY;
		}
	}
	@Override
	protected boolean shouldMoveTo(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		boolean opened=false;
		if(tile instanceof IInventory)
		{
			ITileOpened cap = tile.getCapability(TileCapProvider.OpenedCap, null);
			if(cap!=null)
				opened = cap.playerOpened();
		}
		return opened && !((IInventory) tile).isEmpty();
	}
}
