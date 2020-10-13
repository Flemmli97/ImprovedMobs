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
        this.target = this.living.getAttackTarget();
		//double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);

		if(this.living.ticksExisted % 10 == 0 && this.target != null /*&& motion<maxMotion*/ && this.living.getDistance(this.target) > 1D && this.living.onGround){

			BlockPos blockPos = this.getBlock(this.living);

			if(blockPos == null)
				return false;

			ItemStack item = this.living.getHeldItemMainhand();
			ItemStack itemOff = this.living.getHeldItemOffhand();
			IBlockState block = this.living.world.getBlockState(blockPos);

			if(GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff)){
                this.markedLoc = blockPos;
                this.entityPos = this.living.getPosition();
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
		return this.target != null && this.living != null && this.target.isEntityAlive() && this.living.isEntityAlive() && this.markedLoc != null && this.entityPos != null && this.entityPos.equals(this.living.getPosition())/*motion<maxMotion*/ && this.living.getDistance(this.target) > 1D && (this.target.onGround || !this.living.canEntityBeSeen(this.target));
	}

	@Override
	public void resetTask() {
        this.digTimer = 0;
		if(this.markedLoc != null)
			this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, -1);
        this.markedLoc = null;
	}

	@Override
	public void updateTask() {
		if(this.markedLoc == null || this.living.world.getBlockState(this.markedLoc).getMaterial() == Material.AIR){
            this.digTimer = 0;
			return;
		}
		IBlockState state = this.living.world.getBlockState(this.markedLoc);
        this.digTimer++;

		float str = GeneralHelperMethods.getBlockStrength(this.living, state, this.living.world, this.markedLoc) * (this.digTimer + 1);
		if(str >= 1F){
            this.digTimer = 0;
			ItemStack item = this.living.getHeldItemMainhand();
			ItemStack itemOff = this.living.getHeldItemOffhand();
			boolean canHarvest = GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff);
			if(ConfigHandler.useCoroUtil)
				TileEntityRepairingBlock.replaceBlockAndBackup(this.living.world, this.markedLoc, ConfigHandler.repairTick);
			else
                this.living.world.destroyBlock(this.markedLoc, canHarvest);
            this.markedLoc = null;
            this.living.getNavigator().setPath(this.living.getNavigator().getPathToEntityLiving(this.target), 1D);
		}else{
			if(this.digTimer % 5 == 0){
				SoundType sound = state.getBlock().getSoundType(state, this.living.world, this.markedLoc, this.living);
                this.living.getNavigator().setPath(this.living.getNavigator().getPathToPos(this.markedLoc), 1D);
                this.living.world.playSound(null, this.markedLoc, ConfigHandler.useBlockBreakSound ? sound.getBreakSound() : SoundEvents.BLOCK_NOTE_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
                this.living.swingArm(EnumHand.MAIN_HAND);
                this.living.getLookHelper().setLookPosition(this.markedLoc.getX(), this.markedLoc.getY(), this.markedLoc.getZ(), 0.0F, 0.0F);
                this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, (int) (str) * this.digTimer * 10);
			}
		}
	}

	public BlockPos getBlock(EntityLiving entityLiving) {
		BlockPos partBlockCheck = entityLiving.getPosition();
		BlockPos frontCheck = entityLiving.getPosition().offset(entityLiving.getHorizontalFacing());
		int digWidth = Math.max(1,MathHelper.ceil(entityLiving.width));
		int digHeight = Math.max(1,MathHelper.ceil(entityLiving.height));
		int passMax = digWidth * digWidth * digHeight;
		int x = this.scanTick % digWidth - (digWidth / 2);
		int y = this.scanTick / (digWidth * digWidth);
		int z = (this.scanTick % (digWidth * digWidth)) / digWidth - (digWidth / 2);
		IBlockState notFull = entityLiving.world.getBlockState(partBlockCheck.add(x, y, z));
		IBlockState block = entityLiving.world.getBlockState(frontCheck.add(x, y, z));
		ItemStack item = entityLiving.getHeldItemMainhand();
		ItemStack itemOff = entityLiving.getHeldItemOffhand();
		if(ConfigHandler.breakableBlocks.canBreak(notFull) && (GeneralHelperMethods.canHarvest(notFull, item) || GeneralHelperMethods.canHarvest(notFull, itemOff))){
            this.scanTick = 0;
			return new BlockPos(partBlockCheck.getX() + x, partBlockCheck.getY() + y, frontCheck.getZ() + z);
		}else if(ConfigHandler.breakableBlocks.canBreak(block) && (GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff))){
            this.scanTick = 0;
			return new BlockPos(frontCheck.getX() + x, frontCheck.getY() + y, frontCheck.getZ() + z);
		}
        this.scanTick = (this.scanTick + 1) % passMax;
		return null;
	}
}
