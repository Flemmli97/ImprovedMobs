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
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockBreakGoal extends Goal {

    protected final MobEntity living;
    private LivingEntity target;
    private int scanTick;
    private BlockPos markedLoc;
    private BlockPos entityPos;
    private int digTimer;
    private int cooldown = Config.CommonConfig.breakerInitCooldown;

    public BlockBreakGoal(MobEntity living) {
        this.living = living;
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.living.getAttackTarget();
        if (this.entityPos == null) {
            this.entityPos = this.living.getPosition();
            this.cooldown = Config.CommonConfig.breakerCooldown;
        }
        if (--this.cooldown <= 0) {
            if (!this.entityPos.equals(this.living.getPosition())) {
                this.entityPos = null;
                this.cooldown = Config.CommonConfig.breakerCooldown;
                return false;
            } else if (this.target != null && this.living.getDistance(this.target) > 1D) {// && this.living.isOnGround()) {
                BlockPos blockPos = this.getBlock(this.living);
                if (blockPos == null)
                    return false;
                this.cooldown = Config.CommonConfig.breakerCooldown;
                this.markedLoc = blockPos;
                this.entityPos = this.living.getPosition();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.target != null && this.target.isAlive() && this.living.isAlive() && this.markedLoc != null && this.nearSameSpace(this.entityPos, this.living.getPosition()) && this.living.getDistance(this.target) > 1D; //(this.target.isOnGround() || !this.living.canEntityBeSeen(this.target));
    }

    private boolean nearSameSpace(BlockPos pos1, BlockPos pos2) {
        return pos1 != null && pos2 != null && pos1.getX() == pos2.getX() && pos1.getZ() == pos2.getZ() && Math.abs(pos1.getY() - pos2.getY()) <= 1;
    }

    @Override
    public void resetTask() {
        this.digTimer = 0;
        if (this.markedLoc != null)
            this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, -1);
        this.markedLoc = null;
    }

    @Override
    public void tick() {
        if (this.markedLoc == null || this.living.world.getBlockState(this.markedLoc).getMaterial() == Material.AIR) {
            this.digTimer = 0;
            return;
        }
        BlockState state = this.living.world.getBlockState(this.markedLoc);
        float str = GeneralHelperMethods.getBlockStrength(this.living, state, this.living.world, this.markedLoc);
        str = str == Float.POSITIVE_INFINITY ? 1 : str / (1 + str * 6) * (this.digTimer + 1);
        if (str >= 1F) {
            this.digTimer = 0;
            this.cooldown *= 0.5;
            ItemStack item = this.living.getHeldItemMainhand();
            ItemStack itemOff = this.living.getHeldItemOffhand();
            boolean canHarvest = GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff);
            //if(Config.ServerConfig.useCoroUtil)
            //    TileEntityRepairingBlock.replaceBlockAndBackup(this.living.world, this.markedLoc, ConfigHandler.repairTick);
            //else
            this.living.world.destroyBlock(this.markedLoc, canHarvest);
            this.markedLoc = null;
            this.living.setAIMoveSpeed(0);
            this.living.getNavigator().clearPath();
            this.living.getNavigator().setPath(this.living.getNavigator().pathfind(this.target, 0), 1D);
        } else {
            this.digTimer++;
            if (this.digTimer % 5 == 0) {
                SoundType sound = state.getBlock().getSoundType(state, this.living.world, this.markedLoc, this.living);
                this.living.world.playSound(null, this.markedLoc, Config.CommonConfig.useBlockBreakSound ? sound.getBreakSound() : SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
                this.living.swingArm(Hand.MAIN_HAND);
                this.living.getLookController().setLookPosition(this.markedLoc.getX(), this.markedLoc.getY(), this.markedLoc.getZ(), 0.0F, 0.0F);
                this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, (int) (str) * this.digTimer * 10);
            }
        }
    }

    public BlockPos getBlock(MobEntity entityLiving) {
        ItemStack item = entityLiving.getHeldItemMainhand();
        ItemStack itemOff = entityLiving.getHeldItemOffhand();
        BlockPos pos = entityLiving.getPosition().add(0, 1, 0);
        BlockState state = entityLiving.world.getBlockState(pos);
        if (this.canBreak(entityLiving, state, pos, item, itemOff)) {
            this.scanTick = 0;
            return pos;
        }
        Path path = entityLiving.getNavigator().getPath();
        int digWidth = Math.max(1, MathHelper.ceil(entityLiving.getWidth())) + 1;
        int digHeight = (int) entityLiving.getHeight() + 1;
        if (path != null) {
            PathPoint point = path.getCurrentPathIndex() < path.getCurrentPathLength() ? path.getCurrentPoint() : null;
            if (point != null) {
                BlockPos dir = entityLiving.getPosition().add(-point.x, 0, -point.z);
                int offsetX = dir.getX() > 0 && dir.getZ() != 0 ? 1 : dir.getX() < 0 && dir.getZ() != 0 ? -1 : 0;
                int y = digHeight - this.scanTick / (digWidth * digWidth);
                int x = this.scanTick % digWidth - (digWidth / 2);
                int z = (this.scanTick / digWidth) % digWidth - (digWidth / 2);
                pos = new BlockPos(point.x + x + offsetX, point.y + y, point.z + z);
                BlockPos closest = this.clampedPos(pos, entityLiving.getBoundingBox());
                if (closest.manhattanDistance(pos) <= 1) {
                    state = entityLiving.world.getBlockState(pos);
                    if (this.canBreak(entityLiving, state, pos, item, itemOff)) {
                        this.scanTick = 0;
                        return pos;
                    }
                }
            }
        }
        if (entityLiving.getAttackTarget() != null) {
            BlockPos target = entityLiving.getAttackTarget().getPosition();
            if (target.getY() < entityLiving.getPosY() && target.getX() == pos.getX() && target.getZ() == pos.getZ()) {
                pos = entityLiving.getPosition().down();
                state = entityLiving.world.getBlockState(pos);
                if (this.canBreak(entityLiving, state, pos, item, itemOff)) {
                    this.scanTick = 0;
                    return pos;
                }
            }
        }
        int scanAmount = digWidth * digWidth * (digHeight + 1);
        this.scanTick = (this.scanTick + 1) % scanAmount;
        return null;
    }

    private boolean canBreak(LivingEntity entity, BlockState state, BlockPos pos, ItemStack item, ItemStack itemOff) {
        if (state.getCollisionShapeUncached(entity.world, pos).isEmpty())
            return false;
        return Config.CommonConfig.breakableBlocks.canBreak(state) && (GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff));
    }

    private BlockPos clampedPos(BlockPos point, AxisAlignedBB aabb) {
        return new BlockPos(MathHelper.clamp(point.getX(), (int) Math.floor(aabb.minX), (int) Math.ceil(aabb.maxX)),
                MathHelper.clamp(point.getY(), (int) Math.floor(aabb.minY), (int) Math.ceil(aabb.maxY + 1)),
                MathHelper.clamp(point.getZ(), (int) Math.floor(aabb.minZ), (int) Math.ceil(aabb.maxZ)));
    }
}