package io.github.flemmli97.improvedmobs.common.entities.ai;

import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class LadderClimbGoal extends Goal {

    private final Mob entity;
    private Path path;

    public LadderClimbGoal(Mob entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        if (!this.entity.getNavigation().isDone()) {
            this.path = this.entity.getNavigation().getPath();
            return this.path != null && this.entity.onClimbable();
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.path.isDone())
            return;
        Node target = this.path.getNextNodeIndex() + 1 < this.path.getNodeCount() ? this.path.getNode(this.path.getNextNodeIndex() + 1) : this.path.getNextNode();
        BlockState state = this.entity.getInBlockState();
        BlockPos below = this.entity.blockPosition().below();
        if (!CrossPlatformStuff.INSTANCE.isClimbable(state, this.entity, this.entity.blockPosition())
            && (target.y + 0.2 < this.entity.getY() && !CrossPlatformStuff.INSTANCE.isClimbable(state, this.entity, below))) {
            return;
        }
        double yMotion;
        if (target.y < this.entity.getY())
            yMotion = -0.14;
        else
            yMotion = 0.14;
        Vec3 delta = this.entity.getDeltaMovement().multiply(0.1, 1, 0.1);
        this.entity.setDeltaMovement(delta.x(), yMotion, delta.z());
    }
}
