package io.github.flemmli97.improvedmobs.common.datapack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.datapack.DifficultyAttributeProperty;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class DifficultyAttributeConfig extends SingleFileResources {

    public static final ResourceLocation ID = ImprovedMobs.modRes("difficulty_config");

    public static final DifficultyAttributeProperty DEFAULT = DifficultyAttributeProperty.builder()
            .with(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "min(difficulty * 5 / 250, 5)")
            .with(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "min(difficulty * 3 / 250, 3)")
            .with(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_VALUE, "min(difficulty * 0.1 / 250, 0.1)")
            .with(Attributes.SCALE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "rand(-0.2, 0.2)")
            .with(EntityFlags.ServerSideAttributes.MAGIC_RESISTANCE, "min(difficulty * 0.4 / 250, 0.4)")
            .with(EntityFlags.ServerSideAttributes.PROJECTILE_DAMAGE_MULTIPLIER, "min(difficulty * 2 / 250, 2)")
            .with(EntityFlags.ServerSideAttributes.EXPLOSION_DAMAGE_MULTIPLIER, "min(difficulty * 2 / 250, 2)")
            .build();

    private static DifficultyAttributeConfig INSTANCE;

    private DifficultyAttributeProperty config = DEFAULT;

    private DifficultyAttributeConfig(HolderLookup.Provider provider) {
        super(ImprovedMobs.modRes("attributes"), provider);
    }

    public static DifficultyAttributeConfig create(HolderLookup.Provider provider) {
        DifficultyAttributeConfig.INSTANCE = new DifficultyAttributeConfig(provider);
        return getInstance();
    }

    public static DifficultyAttributeConfig getInstance() {
        return INSTANCE;
    }

    public DifficultyAttributeProperty config() {
        return this.config;
    }

    @Override
    protected void apply(JsonElement element, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (element.isJsonNull())
            return;
        this.config = DifficultyAttributeProperty.CODEC.parse(this.provider.createSerializationContext(JsonOps.INSTANCE), element).getOrThrow();
    }
}
