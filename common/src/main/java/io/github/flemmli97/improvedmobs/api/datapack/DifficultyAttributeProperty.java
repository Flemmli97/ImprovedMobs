package io.github.flemmli97.improvedmobs.api.datapack;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import io.github.flemmli97.improvedmobs.common.utils.Utils;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;
import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.Expression;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DifficultyAttributeProperty {

    public static final Codec<DifficultyAttributeProperty> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), DifficultyAttributeValue.CODEC).fieldOf("attributes").forGetter(d -> d.attributes),
                    Codec.unboundedMap(CodecUtils.stringEnumCodec(EntityFlags.ServerSideAttributes.class, null), ExpressionHolder.CODEC).fieldOf("additional").forGetter(d -> d.attributesExt)
            ).apply(instance, DifficultyAttributeProperty::new));

    private final Map<Holder<Attribute>, DifficultyAttributeValue> attributes;
    private final Map<EntityFlags.ServerSideAttributes, ExpressionHolder> attributesExt;

    public DifficultyAttributeProperty(Map<Holder<Attribute>, DifficultyAttributeValue> attributes, Map<EntityFlags.ServerSideAttributes, ExpressionHolder> attributesExt) {
        this.attributes = attributes;
        this.attributesExt = attributesExt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void apply(LivingEntity entity, VariableMap map, @Nullable DifficultyAttributeProperty overwrite) {
        for (Map.Entry<Holder<Attribute>, DifficultyAttributeValue> entry : this.attributes.entrySet()) {
            if (overwrite != null && overwrite.attributes.containsKey(entry.getKey()))
                continue;
            AttributeInstance inst = entity.getAttribute(entry.getKey());
            if (inst == null || inst.getModifier(Utils.ATTRIBUTE_ID) != null)
                continue;
            inst.addPermanentModifier(new AttributeModifier(Utils.ATTRIBUTE_ID, entry.getValue().expression().value().get(map), entry.getValue().operation()));
            if (entry.getKey().value() == Attributes.MAX_HEALTH.value())
                entity.setHealth(entity.getMaxHealth());
        }
        for (Map.Entry<EntityFlags.ServerSideAttributes, ExpressionHolder> entry : this.attributesExt.entrySet()) {
            if (overwrite != null && overwrite.attributesExt.containsKey(entry.getKey()))
                continue;
            EntityFlags.get(entity).setAttribute(entry.getKey(), entry.getValue().value().get(map));
        }
        if (overwrite != null)
            overwrite.apply(entity, map, null);
    }

    public record DifficultyAttributeValue(AttributeModifier.Operation operation, ExpressionHolder expression) {

        public static final Codec<DifficultyAttributeValue> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(DifficultyAttributeValue::operation),
                        ExpressionHolder.CODEC.fieldOf("value").forGetter(DifficultyAttributeValue::expression)
                ).apply(instance, DifficultyAttributeValue::new));
    }

    public record ExpressionHolder(String expression, ExpValue value) {

        public static final Codec<ExpressionHolder> CODEC = Codec.STRING.xmap(ExpressionHolder::new, ExpressionHolder::expression);

        public ExpressionHolder(String expression) {
            this(expression, Expression.of(expression));
        }
    }

    public static class Builder {

        private final Map<Holder<Attribute>, DifficultyAttributeValue> attributes = new HashMap<>();
        private final Map<EntityFlags.ServerSideAttributes, ExpressionHolder> attributesExt = new HashMap<>();

        public Builder with(Holder<Attribute> atttribute, AttributeModifier.Operation operation, String value) {
            this.attributes.put(atttribute, new DifficultyAttributeValue(operation, new ExpressionHolder(value)));
            return this;
        }

        public Builder with(EntityFlags.ServerSideAttributes atttribute, String value) {
            this.attributesExt.put(atttribute, new ExpressionHolder(value));
            return this;
        }

        public DifficultyAttributeProperty build() {
            return new DifficultyAttributeProperty(ImmutableMap.copyOf(this.attributes), ImmutableMap.copyOf(this.attributesExt));
        }
    }
}
