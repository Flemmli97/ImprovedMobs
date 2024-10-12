package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.BlockPos;

public class NoHeightBlockPosConfig {

    private BlockPos pos = BlockPos.ZERO;

    public NoHeightBlockPosConfig readFromString(String s) {
        String[] split = s.split("-");
        int x;
        int z;
        try {
            x = Integer.parseInt(split[0]);
            z = Integer.parseInt(split[1]);
        } catch (Exception e) {
            ImprovedMobs.LOGGER.error("Error parsing block pos from config {}", e.getMessage());
            return this;
        }
        this.pos = new BlockPos(x, 0, z);
        return this;
    }

    public String writeToString() {
        return this.pos.getX() + "-" + this.pos.getZ();
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
