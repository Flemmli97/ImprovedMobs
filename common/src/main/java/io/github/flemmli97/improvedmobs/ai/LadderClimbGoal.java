package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class LadderClimbGoal extends Goal {

    private final Mob entity;
    private Path path;

    public LadderClimbGoal(Mob entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE));
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
            int y = this.path.getNode(i).y;//this.living.getPosition().getY();
            Node pointNext = this.path.getNode(i + 1);
            BlockState down = this.entity.level().getBlockState(this.entity.blockPosition().below());
            double yMotion;
            if (pointNext.y < y || (pointNext.y == y && !CrossPlatformStuff.INSTANCE.isLadder(down, this.entity, this.entity.blockPosition().below())))
                yMotion = -0.15;
            else
                yMotion = 0.15;
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().multiply(0.1, 1, 0.1));
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, yMotion, 0));
        }
    }
}
