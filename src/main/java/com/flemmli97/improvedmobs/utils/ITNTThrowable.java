package com.flemmli97.improvedmobs.utils;

import net.minecraft.entity.Entity;

public interface ITNTThrowable {

    void shootFromEntity(Entity shooter, float pitch, float yaw, float delta, float velocity, float accuracy);
}
