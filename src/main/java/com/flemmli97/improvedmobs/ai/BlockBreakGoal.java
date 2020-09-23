package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockBreakGoal extends Goal {

    MobEntity living;
    LivingEntity target;
    int scanTick;
    private BlockPos markedLoc;
    private BlockPos entityPos;
    private int digTimer;

    //private static float maxMotion = 2;
    public BlockBreakGoal(MobEntity living) {
        this.living = living;
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.living.getAttackTarget();
        //double motion = MathHelper.sqrt(living.motionX)+MathHelper.sqrt(living.motionZ);

        if(this.living.ticksExisted % 10 == 0 && this.target != null /*&& motion<maxMotion*/ && this.living.getDistance(this.target) > 1D && this.living.isOnGround()){

            BlockPos blockPos = this.getBlock(this.living);

            if(blockPos == null)
                return false;

            ItemStack item = this.living.getHeldItemMainhand();
            ItemStack itemOff = this.living.getHeldItemOffhand();
            BlockState block = this.living.world.getBlockState(blockPos);

            if(GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff)){
                this.markedLoc = blockPos;
                this.entityPos = this.living.getBlockPos();
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
        return this.target != null && this.living != null && this.target.isAlive() && this.living.isAlive() && this.markedLoc != null && this.entityPos != null && this.entityPos.equals(this.living.getBlockPos())/*motion<maxMotion*/ && this.living.getDistance(this.target) > 1D && (this.target.isOnGround() || !this.living.canEntityBeSeen(this.target));
    }

    @Override
    public void resetTask() {
        this.digTimer = 0;
        if(this.markedLoc != null)
            this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, -1);
        this.markedLoc = null;
    }

    @Override
    public void tick() {
        if(this.markedLoc == null || this.living.world.getBlockState(this.markedLoc).getMaterial() == Material.AIR){
            this.digTimer = 0;
            return;
        }
        BlockState state = this.living.world.getBlockState(this.markedLoc);
        this.digTimer++;

        float str = GeneralHelperMethods.getBlockStrength(this.living, state, this.living.world, this.markedLoc) * (this.digTimer + 1);
        if(str >= 1F){
            this.digTimer = 0;
            ItemStack item = this.living.getHeldItemMainhand();
            ItemStack itemOff = this.living.getHeldItemOffhand();
            boolean canHarvest = GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff);
            //if(Config.commonConf.useCoroUtil)
            //    TileEntityRepairingBlock.replaceBlockAndBackup(this.living.world, this.markedLoc, ConfigHandler.repairTick);
            //else
                this.living.world.destroyBlock(this.markedLoc, canHarvest);
            this.markedLoc = null;
            this.living.getNavigator().setPath(this.living.getNavigator().getPathToEntityLiving(this.target, 0), 1D);
        }else{
            if(this.digTimer % 5 == 0){
                SoundType sound = state.getBlock().getSoundType(state, this.living.world, this.markedLoc, this.living);
                this.living.getNavigator().setPath(this.living.getNavigator().getPathToPos(this.markedLoc, 0), 1D);
                this.living.world.playSound(null, this.markedLoc, Config.commonConf.useBlockBreakSound ? sound.getBreakSound() : SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
                this.living.swingArm(Hand.MAIN_HAND);
                this.living.getLookController().setLookPosition(this.markedLoc.getX(), this.markedLoc.getY(), this.markedLoc.getZ(), 0.0F, 0.0F);
                this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, (int) (str) * this.digTimer * 10);
            }
        }
    }

    public BlockPos getBlock(MobEntity entityLiving) {
        BlockPos partBlockCheck = entityLiving.getBlockPos();
        BlockPos frontCheck = entityLiving.getBlockPos().offset(entityLiving.getHorizontalFacing());
        int digWidth = MathHelper.ceil(entityLiving.getWidth());
        int digHeight = MathHelper.ceil(entityLiving.getHeight());
        int passMax = digWidth * digWidth * digHeight;
        int x = this.scanTick % digWidth - (digWidth / 2);
        int y = this.scanTick / (digWidth * digWidth);
        int z = (this.scanTick % (digWidth * digWidth)) / digWidth - (digWidth / 2);
        BlockState notFull = entityLiving.world.getBlockState(partBlockCheck.add(x, y, z));
        BlockState block = entityLiving.world.getBlockState(frontCheck.add(x, y, z));
        ItemStack item = entityLiving.getHeldItemMainhand();
        ItemStack itemOff = entityLiving.getHeldItemOffhand();
        if(Config.commonConf.breakableBlocks.canBreak(notFull) && (GeneralHelperMethods.canHarvest(notFull, item) || GeneralHelperMethods.canHarvest(notFull, itemOff))){
            this.scanTick = 0;
            return partBlockCheck.add(x, y, z);
        }else if(Config.commonConf.breakableBlocks.canBreak(block) && (GeneralHelperMethods.canHarvest(block, item) || GeneralHelperMethods.canHarvest(block, itemOff))){
            this.scanTick = 0;
            return frontCheck.add(x, y, z);
        }
        this.scanTick = (this.scanTick + 1) % passMax;
        return null;
    }
}
