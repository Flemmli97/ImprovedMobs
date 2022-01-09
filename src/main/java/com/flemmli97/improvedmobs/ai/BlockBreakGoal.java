package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakGoal extends Goal {

    protected final MobEntity living;
    private LivingEntity target;
    private BlockPos markedLoc;
    private BlockPos entityPos;
    private int digTimer;
    private int cooldown = Config.CommonConfig.breakerInitCooldown;

    private final List<BlockPos> breakAOE = new ArrayList<>();
    private int breakIndex;

    private final int digHeight;

    public BlockBreakGoal(MobEntity living) {
        this.living = living;
        int digWidth = living.getWidth() < 1 ? 0 : MathHelper.ceil(living.getWidth());
        this.digHeight = (int) living.getHeight() + 1;
        for (int i = digHeight; i >= 0; i--)
            this.breakAOE.add(new BlockPos(0, i, 0));
        //north = neg z
        for (int z = digWidth + 1; z >= -digWidth; z--)
            for (int y = digHeight; y >= 0; y--) {
                for (int x = 0; x <= digWidth; x++) {
                    if (z != 0) {
                        this.breakAOE.add(new BlockPos(x, y, z));
                        if (x != 0)
                            this.breakAOE.add(new BlockPos(-x, y, z));
                    }
                }
            }
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
            } else if (this.target != null && this.living.getDistanceSq(this.target) > 1D) {// && this.living.isOnGround()) {
                BlockPos blockPos = this.getDiggingLocation();
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
        return this.target != null && this.target.isAlive() && this.living.isAlive() && this.markedLoc != null && this.nearSameSpace(this.entityPos, this.living.getPosition()) && this.living.getDistanceSq(this.target) > 1D;
    }

    private boolean nearSameSpace(BlockPos pos1, BlockPos pos2) {
        return pos1 != null && pos2 != null && pos1.getX() == pos2.getX() && pos1.getZ() == pos2.getZ() && Math.abs(pos1.getY() - pos2.getY()) <= 1;
    }

    @Override
    public void resetTask() {
        this.breakIndex = 0;
        if (this.markedLoc != null)
            this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, -1);
        this.markedLoc = null;
    }

    @Override
    public void tick() {
        if (this.markedLoc == null || this.living.world.getBlockState(this.markedLoc).getCollisionShape(this.living.world, this.markedLoc).isEmpty()) {
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
            this.living.world.destroyBlock(this.markedLoc, canHarvest);
            this.markedLoc = null;
            if (!this.aboveTarget()) {
                this.living.setAIMoveSpeed(0);
                this.living.getNavigator().clearPath();
                this.living.getNavigator().setPath(this.living.getNavigator().pathfind(this.target, 0), 1D);
            }
        } else {
            this.digTimer++;
            if (this.digTimer % 5 == 0) {
                SoundType sound = state.getSoundType(this.living.world, this.markedLoc, this.living);
                this.living.world.playSound(null, this.markedLoc, Config.CommonConfig.useBlockBreakSound ? sound.getBreakSound() : SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.BLOCKS, 2F, 0.5F);
                this.living.swingArm(Hand.MAIN_HAND);
                this.living.getLookController().setLookPosition(this.markedLoc.getX(), this.markedLoc.getY(), this.markedLoc.getZ(), 0.0F, 0.0F);
                this.living.world.sendBlockBreakProgress(this.living.getEntityId(), this.markedLoc, (int) (str) * this.digTimer * 10);
            }
        }
    }

    public BlockPos getDiggingLocation() {
        ItemStack item = this.living.getHeldItemMainhand();
        ItemStack itemOff = this.living.getHeldItemOffhand();
        BlockPos pos = this.living.getPosition();
        BlockState state;
        if (this.living.getAttackTarget() != null) {
            Vector3d target = this.living.getAttackTarget().getPositionVec();
            if (this.aboveTarget() && Math.abs(target.x - pos.getX()) <= 1 && Math.abs(target.z - pos.getZ()) <= 1) {
                pos = this.living.getPosition().down();
                state = this.living.world.getBlockState(pos);
                if (this.canBreak(this.living, state, pos, item, itemOff)) {
                    this.breakIndex = 0;
                    return pos;
                }
            }
        }
        Rotation rot = getDigDirection(this.living);
        BlockPos offset = this.breakAOE.get(this.breakIndex);
        offset = new BlockPos(offset.getX(), this.aboveTarget() ? (-(offset.getY() - this.digHeight)) : offset.getY(), offset.getZ());
        pos = pos.add(offset.rotate(rot));
        state = this.living.world.getBlockState(pos);
        if (this.canBreak(this.living, state, pos, item, itemOff)) {
            this.breakIndex = 0;
            return pos;
        }
        this.breakIndex++;
        if (this.breakIndex == this.breakAOE.size())
            this.breakIndex = 0;
        return null;
    }

    private boolean canBreak(LivingEntity entity, BlockState state, BlockPos pos, ItemStack item, ItemStack itemOff) {
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.world, ISelectionContext.forEntity(entity)) && (GeneralHelperMethods.canHarvest(state, item) || GeneralHelperMethods.canHarvest(state, itemOff));
    }

    private boolean aboveTarget() {
        return this.target.getPosY() < this.living.getPosY() + 1.1;
    }

    public static Rotation getDigDirection(MobEntity mob) {
        Path path = mob.getNavigator().getPath();
        if (path != null) {
            PathPoint point = path.getCurrentPathIndex() < path.getCurrentPathLength() ? path.getCurrentPoint() : null;
            if (point != null) {
                Vector3d dir = new Vector3d(point.x + 0.5, mob.getPosY(), point.z + 0.5).subtract(mob.getPositionVec());
                if (Math.abs(dir.x) < Math.abs(dir.z)) {
                    if (dir.z >= 0)
                        return Rotation.NONE;
                    return Rotation.CLOCKWISE_180;
                } else {
                    if (dir.x > 0)
                        return Rotation.COUNTERCLOCKWISE_90;
                    return Rotation.CLOCKWISE_90;
                }
            }
        }
        switch (mob.getHorizontalFacing()) {
            case SOUTH:
                return Rotation.CLOCKWISE_180;
            case EAST:
                return Rotation.CLOCKWISE_90;
            case WEST:
                return Rotation.COUNTERCLOCKWISE_90;
        }
        ;
        return Rotation.NONE;
    }
}