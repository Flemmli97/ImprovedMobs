package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.mixinhelper.INodeBreakable;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NodeEvaluator.class)
public abstract class NodeEvaluatorMixin implements INodeBreakable {

    @Unique
    private boolean improvedMobs$canBreakBlocksIM;

    @Unique
    private boolean improvedMobs$canClimbLadder;

    @Override
    public void improvedMobs$setCanBreakBlocks(boolean flag) {
        this.improvedMobs$canBreakBlocksIM = flag;
    }

    @Override
    public boolean improvedMobs$canBreakBlocks() {
        return this.improvedMobs$canBreakBlocksIM;
    }

    @Override
    public void improvedMobs$setCanClimbLadder(boolean flag) {
        this.improvedMobs$canClimbLadder = flag;
    }

    @Override
    public boolean improvedMobs$canClimbLadder() {
        return this.improvedMobs$canClimbLadder;
    }
}
