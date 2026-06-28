package io.github.flemmli97.improvedmobs.common.config.tags;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.loader.event.TagModifyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;

public class DynamicTags {

    public static TagKey<EntityType<?>> HOSTILE_MOBS = TagKey.create(Registries.ENTITY_TYPE, ImprovedMobs.modRes("hostile_mobs"));
    public static TagKey<EntityType<?>> NON_HOSTILE_MOBS = TagKey.create(Registries.ENTITY_TYPE, ImprovedMobs.modRes("non_hostile_mobs"));
    public static TagKey<EntityType<?>> NEUTRAL_MOBS = TagKey.create(Registries.ENTITY_TYPE, ImprovedMobs.modRes("neutral_mobs"));
    public static TagKey<EntityType<?>> ANIMAL_MOBS = TagKey.create(Registries.ENTITY_TYPE, ImprovedMobs.modRes("animal_mobs"));

    public static void init() {
        TagModifyEvent.INSTANCE.registerListener(Registries.ENTITY_TYPE, handler -> {
            DummyLevel level = new DummyLevel(handler.registryAccess());
            BuiltInRegistries.ENTITY_TYPE.holders().forEach(type -> {
                try {
                    Entity entity = type.value().create(level);
                    if (entity instanceof Mob && entity instanceof Enemy) {
                        handler.add(HOSTILE_MOBS, type);
                    }
                    if (entity instanceof Mob && !(entity instanceof Enemy)) {
                        handler.add(NON_HOSTILE_MOBS, type);
                    }
                    if (entity instanceof NeutralMob) {
                        handler.add(NEUTRAL_MOBS, type);
                    }
                    if (entity instanceof Animal) {
                        handler.add(ANIMAL_MOBS, type);
                    }
                } catch (Exception e) {
                    ImprovedMobs.LOGGER.error("Error during default entity config for EntityType {}, skipping this type. Cause: {}", type.key(), e.getMessage());
                }
            });
        });
    }
}
