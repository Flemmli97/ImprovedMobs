package com.flemmli97.improvedmobs.entity.ai;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;

import CoroUtil.block.TileEntityRepairingBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EntityAIBlockBreaking extends EntityAIBase {

	EntityLiving living;
	EntityLivingBase target;
	int scanTick;
	private BlockPos markedLoc;
	private BlockPos entityPos;
	private int digTimer;

	//private static float maxMotion = 2;
	public EntityAIBlockBreaking(EntityLiving living) {
		this.living = living;
	}

	@Override
	public boolean shouldExecute() {
		target = living.getAttackTarget();
		//double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);

		if(living.ticksExisted % 10 == 0 && target != null /*&& motion<maxMotion*/ && living.getDistance(target) > 1D && living.onGround){

			BlockPos blockPos = this.getBlock(living);

			if(blockPos == null)
				return false;

			ItemStack item = living.getHeldItemMainhand();
			ItemStack itemOff = living.getHeldItemOffhand();
			IBlockState block = living.world.getBlockState(blockPos);

			if(GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff)){
				markedLoc = blockPos;
				entityPos = living.getPosition();
				return true;
			}else{
				return false;
			}
		}

		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		//double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);
		return target != null && living != null && target.isEntityAlive() && living.isEntityAlive() && markedLoc != null && entityPos != null && entityPos.equals(living.getPosition())/*motion<maxMotion*/ && living.getDistance(target) > 1D && (target.onGround || !living.canEntityBeSeen(target));
	}

	@Override
	public void resetTask() {
		digTimer = 0;
		if(this.markedLoc != null)
			this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, -1);
		markedLoc = null;
	}

	@Override
	public void updateTask() {
		if(markedLoc == null || living.world.getBlockState(markedLoc).getMaterial() == Material.AIR){
			digTimer = 0;
			return;
		}
		IBlockState state = living.world.getBlockState(markedLoc);
		digTimer++;

		float str = GeneralHelperMethods.getBlockStrength(this.living, state, living.world, markedLoc) * (digTimer + 1);
		if(str >= 1F){
			digTimer = 0;
			ItemStack item = living.getHeldItemMainhand();
			ItemStack itemOff = living.getHeldItemOffhand();
			boolean canHarvest = GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff);
			if(ConfigHandler.useCoroUtil)
				TileEntityRepairingBlock.replaceBlockAndBackup(living.world, markedLoc, ConfigHandler.repairTick);
			else
				living.world.destroyBlock(markedLoc, canHarvest);
			markedLoc = null;
			living.getNavigator().setPath(living.getNavigator().getPathToEntityLiving(target), 1D);
		}else{
			if(digTimer % 5 == 0){
				SoundType sound = state.getBlock().getSoundType(state, living.world, markedLoc, living);
				living.getNavigator().setPath(living.getNavigator().getPathToPos(markedLoc), 1D);
				living.world.playSound(null, markedLoc, ConfigHandler.useBlockBreakSound ? sound.getBreakSound() : SoundEvents.BLOCK_NOTE_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
				living.swingArm(EnumHand.MAIN_HAND);
				living.getLookHelper().setLookPosition(markedLoc.getX(), markedLoc.getY(), markedLoc.getZ(), 0.0F, 0.0F);
				living.world.sendBlockBreakProgress(living.getEntityId(), markedLoc, (int) (str) * digTimer * 10);
			}
		}
	}

	public BlockPos getBlock(EntityLiving entityLiving) {
		BlockPos i = null;
		BlockPos partBlockCheck = entityLiving.getPosition();
		BlockPos frontCheck = entityLiving.getPosition().offset(entityLiving.getHorizontalFacing());
		int digWidth = MathHelper.ceil(entityLiving.width);
		int digHeight = MathHelper.ceil(entityLiving.height);
		int passMax = digWidth * digWidth * digHeight;
		int x = scanTick % digWidth - (digWidth / 2);
		int y = scanTick / (digWidth * digWidth);
		int z = (scanTick % (digWidth * digWidth)) / digWidth - (digWidth / 2);
		IBlockState notFull = entityLiving.world.getBlockState(partBlockCheck.add(x, y, z));
		IBlockState block = entityLiving.world.getBlockState(frontCheck.add(x, y, z));
		ItemStack item = entityLiving.getHeldItemMainhand();
		ItemStack itemOff = entityLiving.getHeldItemOffhand();
		if(ConfigHandler.breakableBlocks.canBreak(notFull) && (GeneralHelperMethods.canHarvest(notFull, item) || GeneralHelperMethods.canHarvest(notFull, itemOff))){
			scanTick = 0;
			i = new BlockPos(partBlockCheck.getX() + x, partBlockCheck.getY() + y, frontCheck.getZ() + z);
			return i;
		}else if(ConfigHandler.breakableBlocks.canBreak(block) && (GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff))){
			scanTick = 0;
			i = new BlockPos(frontCheck.getX() + x, frontCheck.getY() + y, frontCheck.getZ() + z);
			return i;
		}
		scanTick = (scanTick + 1) % passMax;
		return null;
	}
}
