package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.api.config.IConfigValue;
import net.minecraft.core.BlockPos;

public class NoHeightBlockPosConfig implements IConfigValue<NoHeightBlockPosConfig> {

    private BlockPos pos = BlockPos.ZERO;

    @Override
    public NoHeightBlockPosConfig readFromString(String s) {
        String[] split = s.split("-");
        int x;
        try {
            x = Integer.parseInt(split[0]);
        } catch (Exception e) {
            ImprovedMobs.logger.error("Error parsing block pos from config " + e.getMessage());
            return this;
        }
        ;
        int z;
        try {
            z = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            ImprovedMobs.logger.error("Error parsing block pos from config " + e.getMessage());
            return this;
        }
        ;
        this.pos = new BlockPos(x, 0, z);
        return this;
    }

    @Override
    public String writeToString() {
        return this.pos.getX() + "-" + this.pos.getZ();
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
