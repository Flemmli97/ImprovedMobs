package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.mixinhelper.INodeBreakable;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NodeEvaluator.class)
public abstract class NodeEvaluatorMixin implements INodeBreakable {

    @Unique
    private boolean canBreakBlocksIM;

    @Unique
    private boolean canClimbLadder;

    @Override
    public void setCanBreakBlocks(boolean flag) {
        this.canBreakBlocksIM = flag;
    }

    @Override
    public boolean canBreakBlocks() {
        return this.canBreakBlocksIM;
    }

    @Override
    public void setCanClimbLadder(boolean flag) {
        this.canClimbLadder = flag;
    }

    @Override
    public boolean canClimbLadder() {
        return this.canClimbLadder;
    }
}
