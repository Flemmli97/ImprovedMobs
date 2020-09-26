package com.flemmli97.improvedmobs.ai;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;

import java.util.EnumSet;

public class LadderClimbGoal extends Goal {

    private final MobEntity entity;
    private Path path;

    public LadderClimbGoal(MobEntity entity) {
        this.entity = entity;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute() {
        if (!this.entity.getNavigator().noPath()) {
            this.path = this.entity.getNavigator().getPath();
            return this.path != null && this.entity.isOnLadder();
        }
        return false;
    }

    @Override
    public void tick() {
        int i = this.path.getCurrentPathIndex();
        if (i + 1 < this.path.getCurrentPathLength()) {
            int y = this.path.getPathPointFromIndex(i).y;//this.living.getPosition().getY();
            PathPoint pointNext = this.path.getPathPointFromIndex(i + 1);
            BlockState down = this.entity.world.getBlockState(this.entity.getBlockPos().down());
            double yMotion;
            if (pointNext.y < y || (pointNext.y == y && !down.getBlock().isLadder(down, this.entity.world, this.entity.getBlockPos().down(), this.entity)))
                yMotion = -0.15;
            else
                yMotion = 0.15;
            this.entity.setMotion(this.entity.getMotion().mul(0.5, 1, 0.5));
            this.entity.setMotion(this.entity.getMotion().add(0, yMotion, 0));
        }
    }
}
