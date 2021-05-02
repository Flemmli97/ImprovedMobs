package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import net.minecraft.pathfinding.NodeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NodeProcessor.class)
public abstract class LadderFlagMixin implements ILadderFlagNode {

    @Unique
    private boolean canClimbLadder;

    @Override
    public void setCanClimbLadder(boolean flag) {
        this.canClimbLadder = flag;
    }

    @Override
    public boolean canClimbLadder() {
        return this.canClimbLadder;
    }
}
