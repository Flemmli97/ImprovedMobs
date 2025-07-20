package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.improvedmobs.ImprovedMobs;

public class Pos2iConfig {

    private Pos2i pos = new Pos2i(0, 0);

    public Pos2i getPos() {
        return this.pos;
    }

    public void read(String config) {
        String[] split = config.split("-");
        if (split.length != 2)
            return;
        try {
            int x = Integer.parseInt(split[0]);
            int z = Integer.parseInt(split[1]);
            this.pos = new Pos2i(x, z);
        } catch (Exception e) {
            ImprovedMobs.LOGGER.error("Error parsing block pos from config {}", e.getMessage());
        }
    }

    public String write() {
        return this.pos.x() + "-" + this.pos.z();
    }

    public record Pos2i(int x, int z) {

    }
}
