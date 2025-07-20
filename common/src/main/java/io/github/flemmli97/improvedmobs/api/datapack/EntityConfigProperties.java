package io.github.flemmli97.improvedmobs.api.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public record EntityConfigProperties(EntityTypeValue type, Optional<EnumSet<DifficultyFeatures>> enabledFeatures,
                                     ConfigurableProperty<DifficultyAttributeProperty> attributes) {

    public static final Codec<EntityConfigProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(EntityTypeValue.CODEC.fieldOf("type").forGetter(EntityConfigProperties::type),
                    CodecUtils.stringEnumCodec(DifficultyFeatures.class, null).listOf().optionalFieldOf("features").forGetter(d -> d.enabledFeatures().map(List::copyOf)),
                    ConfigurableProperty.codecFor(DifficultyAttributeProperty.CODEC).fieldOf("attributes").forGetter(EntityConfigProperties::attributes)
            ).apply(instance, (type, features, attributes) ->
                    new EntityConfigProperties(type, features.map(EnumSet::copyOf), attributes)));

    public record ConfigurableProperty<T>(T val, boolean replace) {

        public static <T> Codec<ConfigurableProperty<T>> codecFor(Codec<T> codec) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(codec.fieldOf("value").forGetter(ConfigurableProperty::val),
                                    Codec.BOOL.fieldOf("replace_config").forGetter(ConfigurableProperty::replace))
                            .apply(instance, ConfigurableProperty::new));
        }
    }
}
