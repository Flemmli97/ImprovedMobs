package io.github.flemmli97.improvedmobs.common.config.values;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TargetMapConfig {

    private final List<String> config = new ArrayList<>();
    private boolean initialized;

    private final Map<EntityType<?>, Predicate<EntityType<?>>> map = new HashMap<>();

    @Nullable
    public Predicate<EntityType<?>> get(EntityType<?> type) {
        this.initialize();
        return this.map.get(type);
    }

    private void initialize() {
        if (this.initialized)
            return;
        this.initialized = true;
        Map<EntityType<?>, PredicateBuilder> direct = new HashMap<>();
        Map<HolderSet<EntityType<?>>, PredicateBuilder> tags = new HashMap<>();
        for (String value : this.config) {
            String[] sub = value.replace(" ", "").split("-");
            if (sub.length < 2)
                continue;
            String source = sub[0];
            Consumer<Either<TagKey<EntityType<?>>, EntityType<?>>> cons;
            if (source.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(source.substring(1)));
                cons = either -> BuiltInRegistries.ENTITY_TYPE.getTag(tag)
                        .ifPresent(named -> either
                                .ifLeft(t -> tags.computeIfAbsent(named, key -> new PredicateBuilder()).appendTag(t))
                                .ifRight(t -> tags.computeIfAbsent(named, key -> new PredicateBuilder()).appendType(t)));
            } else {
                Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(source));
                if (type.isEmpty())
                    continue;
                cons = either -> either
                        .ifLeft(t -> direct.computeIfAbsent(type.get(), key -> new PredicateBuilder()).appendTag(t))
                        .ifRight(t -> direct.computeIfAbsent(type.get(), key -> new PredicateBuilder()).appendType(t));
            }
            if (sub[1].startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(sub[1].substring(1)));
                if (BuiltInRegistries.ENTITY_TYPE.getTag(tag).isPresent()) {
                    cons.accept(Either.left(tag));
                }
            } else {
                EntityType<?> target = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(sub[1])).orElse(null);
                if (target == null) {
                    ImprovedMobs.LOGGER.error("Entity {} does not exist/is not registered", sub[1]);
                    continue;
                }
                cons.accept(Either.right(target));
            }
        }
        direct.forEach((type, pred) -> this.map.put(type, pred.build()));
        tags.forEach((set, pred) -> set.forEach(h -> this.map.put(h.value(), pred.build())));
    }

    public void tagReloaded() {
        this.map.clear();
        this.initialized = false;
        this.initialize();
    }

    public void read(List<String> config) {
        this.map.clear();
        this.config.clear();
        this.config.addAll(config);
        this.initialized = false;
    }

    public List<String> write() {
        return List.copyOf(this.config);
    }

    public static String[] use() {
        return new String[]{
                "List for of pairs containing which mobs auto target others.",
                "Syntax is <source-target> where",
                "  source: is the mob that should target something. This can be either an entity or a tag",
                "  target: the mob source should target. This can be either an entity or a tag",
                "Examples: ",
                "minecraft:zombie-minecraft:skeleton makes all zombies target skeletons",
                "minecraft:zombie-#minecraft:raiders makes all zombies target raid entities"
        };
    }

    private static class PredicateBuilder {

        private final List<EntityType<?>> direct = new ArrayList<>();
        private final List<TagKey<EntityType<?>>> tags = new ArrayList<>();

        public void appendType(EntityType<?> type) {
            this.direct.add(type);
        }

        public void appendTag(TagKey<EntityType<?>> tag) {
            this.tags.add(tag);
        }

        public Predicate<EntityType<?>> build() {
            Set<EntityType<?>> direct = ImmutableSet.copyOf(this.direct);
            List<TagKey<EntityType<?>>> tags = ImmutableList.copyOf(this.tags);
            return type -> direct.contains(type) || tags.stream().anyMatch(type::is);
        }
    }
}
