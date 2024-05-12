package io.github.flemmli97.improvedmobs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterNavigation extends GroundPathNavigation {

    public WaterNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new SwimNodeEvaluator(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isInLiquid();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
    }

    @Override
    protected double getGroundY(Vec3 vec) {
        return vec.y;
    }

    @Override
    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
        Vec3 vec3 = new Vec3(posVec32.x, posVec32.y + (double) this.mob.getBbHeight() * 0.5, posVec32.z);
        return this.level.clip(new ClipContext(posVec31, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        return !this.level.getBlockState(pos).isSolidRender(this.level, pos);
    }

    @Override
    public void setCanFloat(boolean canSwim) {
    }
}
