package io.github.flemmli97.improvedmobs.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ServersideRegister {

    private static final Map<ResourceLocation, Function<Level, Entity>> ENTITIES = new HashMap<>();

    public static Optional<Entity> createOf(ResourceLocation id, Level level, CompoundTag tag) {
        Function<Level, Entity> f = ENTITIES.get(id);
        if (f != null) {
            Entity e = f.apply(level);
            e.load(tag);
            return Optional.of(e);
        }
        return Optional.empty();
    }

    static {
        ENTITIES.put(FlyingSummonEntity.SUMMONED_FLYING_ID, FlyingSummonEntity::new);
        ENTITIES.put(AquaticSummonEntity.SUMMONED_AQUATIC_ID, AquaticSummonEntity::new);
    }
}
