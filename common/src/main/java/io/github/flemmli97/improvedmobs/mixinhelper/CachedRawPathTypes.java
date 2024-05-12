package io.github.flemmli97.improvedmobs.mixinhelper;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.pathfinder.PathType;

public interface CachedRawPathTypes {

    Long2ObjectMap<PathType> getPathTypeByPosCacheRaw();
}
