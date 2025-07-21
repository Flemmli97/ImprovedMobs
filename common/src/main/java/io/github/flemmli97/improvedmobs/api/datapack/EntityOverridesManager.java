package io.github.flemmli97.improvedmobs.api.datapack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.common.datapack.DifficultyAttributeConfig;
import io.github.flemmli97.improvedmobs.common.datapack.SingleFileResources;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityOverridesManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ImprovedMobs.modRes("entity_configs");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static EntityOverridesManager INSTANCE;

    private final HolderLookup.Provider provider;

    private Set<EntityConfigProperties> unresolved = ImmutableSet.of();
    private boolean resolved;
    private Map<EntityType<?>, EntityConfigProperties> properties = ImmutableMap.of();

    private EntityOverridesManager(HolderLookup.Provider provider) {
        super(SingleFileResources.GSON, DIRECTORY);
        this.provider = provider;
    }

    public static EntityOverridesManager create(HolderLookup.Provider provider) {
        EntityOverridesManager.INSTANCE = new EntityOverridesManager(provider);
        return getInstance();
    }

    public static EntityOverridesManager getInstance() {
        return INSTANCE;
    }

    public void applyAttributesTo(LivingEntity entity, VariableMap map) {
        this.resolve();
        EntityConfigProperties properties = this.properties.get(entity.getType());
        if (properties != null) {
            if (properties.attributes().replace())
                properties.attributes().val().apply(entity, map, null);
            else
                DifficultyAttributeConfig.getInstance().config().apply(entity, map, properties.attributes().val());
        } else {
            DifficultyAttributeConfig.getInstance().config().apply(entity, map, null);
        }
    }

    public OverrideState isEnabled(LivingEntity entity, DifficultyFeatures feature) {
        this.resolve();
        EntityConfigProperties properties = this.properties.get(entity.getType());
        if (properties == null || properties.enabledFeatures().isEmpty())
            return OverrideState.DEFAULT;
        EnumSet<DifficultyFeatures> set = properties.enabledFeatures().get();
        return set.contains(DifficultyFeatures.REVERSE) ^ (set.contains(DifficultyFeatures.ALL) || set.contains(feature)) ? OverrideState.ALLOW : OverrideState.DENY;
    }

    public OverrideState canBreak(LivingEntity entity, BlockState state) {
        this.resolve();
        EntityConfigProperties properties = this.properties.get(entity.getType());
        if (properties == null)
            return OverrideState.DEFAULT;
        if (properties.breakableBlocks().val().contains(state.getBlockHolder()))
            return OverrideState.ALLOW;
        return properties.breakableBlocks().replace() ? OverrideState.DENY : OverrideState.DEFAULT;
    }

    public void resolve() {
        if (!this.resolved) {
            this.resolved = true;
            Map<EntityType<?>, EntityConfigProperties> direct = new HashMap<>();
            Map<EntityType<?>, EntityConfigProperties> tags = new HashMap<>();
            Map<EntityType<?>, EntityConfigProperties> namespace = new HashMap<>();
            BuiltInRegistries.ENTITY_TYPE.holders().forEach(type -> {
                this.unresolved.forEach(props -> {
                    if (props.type().isDirect(type))
                        direct.put(type.value(), props);
                    if (props.type().isTag(type))
                        tags.put(type.value(), props);
                    if (props.type().isNamespace(type))
                        namespace.put(type.value(), props);
                });
            });
            tags.forEach((type, prop) -> {
                if (!direct.containsKey(type))
                    direct.put(type, prop);
            });
            namespace.forEach((type, prop) -> {
                if (!direct.containsKey(type))
                    direct.put(type, prop);
            });
            this.properties = ImmutableMap.copyOf(direct);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
        this.resolved = false;
        ImmutableSet.Builder<EntityConfigProperties> builder = ImmutableSet.builder();
        DynamicOps<JsonElement> ops = this.provider.createSerializationContext(JsonOps.INSTANCE);
        Set<ResourceLocation> overrides = new HashSet<>();
        data.forEach((res, element) -> {
            try {
                builder.add(EntityConfigProperties.CODEC.parse(ops, element).getOrThrow());
                overrides.add(res);
            } catch (Exception ex) {
                ImprovedMobs.LOGGER.error("Couldn't parse entity config json {} {}", res, ex);
                ex.fillInStackTrace();
            }
        });
        if (!overrides.isEmpty())
            ImprovedMobs.LOGGER.info("Following entity overrides are loaded: {}", overrides);
        this.unresolved = builder.build();
    }

    public enum OverrideState {
        DEFAULT,
        ALLOW,
        DENY
    }
}
