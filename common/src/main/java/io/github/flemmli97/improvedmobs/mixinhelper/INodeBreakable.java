package io.github.flemmli97.improvedmobs.mixinhelper;

public interface INodeBreakable {

    void setCanBreakBlocks(boolean flag);

    boolean canBreakBlocks();

    void setCanClimbLadder(boolean flag);

    boolean canClimbLadder();
}
