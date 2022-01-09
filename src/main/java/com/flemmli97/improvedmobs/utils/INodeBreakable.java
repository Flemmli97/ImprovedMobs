package com.flemmli97.improvedmobs.utils;

public interface INodeBreakable {

    void setCanBreakBlocks(boolean flag);

    boolean canBreakBlocks();

    void setCanClimbLadder(boolean flag);

    boolean canClimbLadder();
}
