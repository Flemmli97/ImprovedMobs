package com.flemmli97.improvedmobs.mixin.pathfinding;

import com.flemmli97.improvedmobs.utils.INodeBreakable;
import net.minecraft.pathfinding.NodeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NodeProcessor.class)
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
