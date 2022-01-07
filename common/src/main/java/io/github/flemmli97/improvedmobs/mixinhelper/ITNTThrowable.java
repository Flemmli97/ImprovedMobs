package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.world.entity.Entity;

public interface ITNTThrowable {

    void shootFromEntity(Entity shooter, float pitch, float yaw, float delta, float velocity, float accuracy);
}
