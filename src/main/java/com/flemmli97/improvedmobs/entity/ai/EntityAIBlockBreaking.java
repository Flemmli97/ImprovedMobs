 package com.flemmli97.improvedmobs.entity.ai;

import com.flemmli97.improvedmobs.handler.ConfigHandler;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EntityAIBlockBreaking extends EntityAIBase{

	EntityLiving living;
	EntityLivingBase target;
	int scanTick;
	private BlockPos markedLoc;
	private int digTick;
	
	public EntityAIBlockBreaking(EntityLiving living)
	{
		this.living=living;
	}
	@Override
	public boolean shouldExecute() {
		target = living.getAttackTarget();
		double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);
		if(living.ticksExisted%20 == 0 && target != null && motion<0.5 && living.getDistance(target) > 1D && target.onGround)
		{

			BlockPos blockPos = this.getBlock(living);

			if(blockPos == null)
			{

				return false;
			}

			ItemStack item = living.getHeldItemMainhand();
			ItemStack itemOff = living.getHeldItemOffhand();
			IBlockState block = living.world.getBlockState(blockPos);
			
			if(GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff))
			{
				markedLoc = blockPos;
				return true;
			} 
			else
			{
				return false;
			}
		}
		
		return false;
	}
	@Override
	public boolean shouldContinueExecuting() {
		double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);
		return target != null && living != null && target.isEntityAlive() && living.isEntityAlive() && markedLoc != null && motion<0.5 && living.getDistance(target) > 1D && (target.onGround || !living.canEntityBeSeen(target));
	}

	@Override
	public void resetTask() {
		markedLoc = null;
		digTick = 0;
	}

	@Override
	public void updateTask() {
		if(markedLoc == null || living.world.getBlockState(markedLoc).getMaterial() == Material.AIR)
		{
			digTick = 0;
			return;
		}
		IBlockState state = living.world.getBlockState(markedLoc);
		digTick++;
		
		float str = GeneralHelperMethods.getBlockStrength(this.living, state, living.world, markedLoc, false) * (digTick + 1);
		if(str >= 1F)
		{
			digTick = 0;
			
			ItemStack item = living.getHeldItemMainhand();
			ItemStack itemOff = living.getHeldItemOffhand();
			boolean canHarvest = GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff);
			living.world.destroyBlock(markedLoc, canHarvest);
			markedLoc = null;
			living.getNavigator().setPath(living.getNavigator().getPathToEntityLiving(target), 1D);
		} else
		{
			if(digTick%5 == 0)
			{
				
				living.getNavigator().setPath(living.getNavigator().getPathToPos(markedLoc), 1D);
				living.world.playSound(null, markedLoc, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
				living.swingArm(EnumHand.MAIN_HAND);
				living.getLookHelper().setLookPosition(markedLoc.getX(), markedLoc.getY(), markedLoc.getZ(), 0.0F, 0.0F);
				living.world.sendBlockBreakProgress(living.getEntityId(), markedLoc, (int)(str)*digTick*10);
			}
		}
	}
	
	public BlockPos getBlock(EntityLiving entityLiving)
	{
		BlockPos i =null;
		//create copy from the pathing of entity
		NewPathNavigateGround nav = new NewPathNavigateGround(entityLiving, entityLiving.world);
		Path path = null;
		if(entityLiving.getAttackTarget()!=null)
			path = nav.getPathToEntityLiving(entityLiving.getAttackTarget());
		if(path!=null)
		{
	        int index = path.getCurrentPathIndex();
	        PathPoint point1 = path.getPathPointFromIndex(index);
	        PathPoint point2 = path.getPathPointFromIndex(index+1);
	        int digWidth = MathHelper.ceil(entityLiving.width);
	        int digHeight = MathHelper.ceil(entityLiving.height);
	        int passMax = digWidth * digWidth * digHeight;
	        int x = scanTick%digWidth - (digWidth/2);
	        int y = scanTick/(digWidth * digWidth);
	        int z = (scanTick%(digWidth * digWidth))/digWidth - (digWidth/2);
	        
	        IBlockState testDoor = entityLiving.world.getBlockState(new BlockPos(point1.x, point1.y, point1.z));
	        IBlockState block = entityLiving.world.getBlockState(new BlockPos(point2.x+x, point2.y+y, point2.z+z));
			if(testDoor instanceof BlockFenceGate ||(testDoor instanceof BlockDoor && testDoor.getMaterial() != Material.IRON))
			{
				scanTick = 0;
				int yCoord = point1.y;
				if(testDoor instanceof BlockDoor)
					yCoord+=1;
				i = new BlockPos(point1.x, yCoord, point1.z);
				return i;
			}
			else if(block.getMaterial()!=Material.AIR)
	        {
				ItemStack item = entityLiving.getHeldItemMainhand();
				ItemStack itemOff = entityLiving.getHeldItemMainhand();

				if(!GeneralHelperMethods.isBlockBreakable(block.getBlock(), ConfigHandler.blockBreakName))
				{
					scanTick = (scanTick + 1)%passMax;
					return null;
				}
				
				if(GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff))
				{
					scanTick = 0;
					i = new BlockPos(point2.x+x, point2.y+y, point2.z+z);
					return i;
				} 
				else
				{
					scanTick = (scanTick + 1)%passMax;
					return null;
				}
	        }
			scanTick = (scanTick + 1)%passMax;
	        return null;
		}
        return null;
        
	}
}
