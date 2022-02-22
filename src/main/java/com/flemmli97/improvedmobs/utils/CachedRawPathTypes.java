package com.flemmli97.improvedmobs.utils;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.pathfinding.PathNodeType;

public interface CachedRawPathTypes {

    Long2ObjectMap<PathNodeType> getPathTypeByPosCacheRaw();
}
