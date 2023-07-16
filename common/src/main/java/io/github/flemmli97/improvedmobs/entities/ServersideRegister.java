package io.github.flemmli97.improvedmobs.entities;

import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ServersideRegister {

    private static final Map<ResourceLocation, Function<Level, Entity>> ENTITIES = new HashMap<>();

    public static void replaceEntity(Entity entity) {
        EntityFlags flags = EntityFlags.get(entity);
        if (flags.serverSideEntityID != null) {
            //Needs a slight delay. Else client gets ghost entities
            entity.getServer().tell(new TickTask(1, () -> {
                Function<Level, Entity> f = ENTITIES.get(flags.serverSideEntityID);
                if (f != null) {
                    Entity newE = f.apply(entity.level());
                    CompoundTag tag = entity.saveWithoutId(new CompoundTag());
                    tag.remove(Entity.UUID_TAG);
                    newE.load(tag);
                    entity.discard();
                    entity.level().addFreshEntity(newE);
                    if (entity.getFirstPassenger() != null)
                        entity.getFirstPassenger().startRiding(newE);
                }
            }));
        }
    }

    static {
        ENTITIES.put(FlyingSummonEntity.SUMMONED_FLYING_ID, FlyingSummonEntity::new);
        ENTITIES.put(AquaticSummonEntity.SUMMONED_AQUATIC_ID, AquaticSummonEntity::new);
    }
}
