package io.github.flemmli97.improvedmobs.neoforge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.api.datapack.DifficultyAttributeProperty;
import io.github.flemmli97.improvedmobs.api.datapack.EntityConfigProperties;
import io.github.flemmli97.improvedmobs.api.datapack.EntityTypeValue;
import io.github.flemmli97.improvedmobs.api.datapack.provider.EntityOverridesProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This is purely for testing
 */
public class EntityOverrideTest extends EntityOverridesProvider {

    public EntityOverrideTest(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, ImprovedMobs.MODID, provider);
    }

    @Override
    protected void add(HolderLookup.Provider provider) {
        this.add(ImprovedMobs.modRes("zombie_test"), new EntityConfigProperties(
                EntityTypeValue.ofType(EntityType.ZOMBIE), Optional.of(EnumSet.of(DifficultyFeatures.ATTRIBUTES)),
                new EntityConfigProperties.ConfigurableProperty<>(DifficultyAttributeProperty.builder()
                        .with(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, "100")
                        .build(), true)
        ));
    }
}
