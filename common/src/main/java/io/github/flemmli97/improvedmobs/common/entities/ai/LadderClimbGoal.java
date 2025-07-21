package io.github.flemmli97.improvedmobs.common.entities.ai;

import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
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
        int i = this.path.getNextNodeIndex();
        if (i + 1 < this.path.getNodeCount()) {
            int y = this.path.getNode(i).y;
            Node pointNext = this.path.getNode(i + 1);
            BlockState down = this.entity.level().getBlockState(this.entity.blockPosition().below());
            double yMotion;
            if (pointNext.y < y || (pointNext.y == y && !CrossPlatformStuff.INSTANCE.isClimbable(down, this.entity, pointNext.asBlockPos())))
                yMotion = -0.14;
            else
                yMotion = 0.14;
            Vec3 delta = this.entity.getDeltaMovement().multiply(0.1, 1, 0.1);
            this.entity.setDeltaMovement(delta.x(), yMotion, delta.z());
        }
    }
}
