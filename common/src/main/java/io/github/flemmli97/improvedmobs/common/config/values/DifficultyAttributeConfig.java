package io.github.flemmli97.improvedmobs.common.config.values;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.utils.Utils;
import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.Expression;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DifficultyAttributeConfig {

    private static final Function<String, AttributeModifier.Operation> LOOKUP = StringRepresentable.createNameLookup(AttributeModifier.Operation.values(), Function.identity());

    private final List<String> config = new ArrayList<>();
    private boolean initialized;

    private final Map<Holder<Attribute>, Pair<AttributeModifier.Operation, ExpValue>> attributes = new HashMap<>();

    public DifficultyAttributeConfig(String... defaultVal) {
        this.config.addAll(List.of(defaultVal));
    }

    public DifficultyAttributeConfig(InitHolder... defaultVal) {
        this.config.addAll(this.from(defaultVal));
    }

    public void apply(LivingEntity entity, VariableMap map) {
        this.initialize();
        for (Map.Entry<Holder<Attribute>, Pair<AttributeModifier.Operation, ExpValue>> entry : this.attributes.entrySet()) {
            AttributeInstance inst = entity.getAttribute(entry.getKey());
            if (inst == null || inst.getModifier(Utils.ATTRIBUTE_ID) != null)
                continue;
            inst.addPermanentModifier(new AttributeModifier(Utils.ATTRIBUTE_ID, entry.getValue().getSecond().get(map), entry.getValue().getFirst()));
        }
    }

    private void initialize() {
        if (this.initialized)
            return;
        this.initialized = true;
        for (String value : this.config) {
            String[] sub = value.split(";");
            if (sub.length != 3)
                continue;
            BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(sub[0]))
                    .ifPresent(att -> {
                        AttributeModifier.Operation op = LOOKUP.apply(sub[1]);
                        if (op == null) {
                            ImprovedMobs.LOGGER.error("No such operation {}", sub[1]);
                        } else {
                            this.attributes.put(att, Pair.of(op, Expression.of(sub[2])));
                        }
                    });
        }
    }

    public void read(List<String> arr) {
        this.config.clear();
        this.config.addAll(arr);
        this.attributes.clear();
        this.initialized = false;
    }

    private List<String> from(InitHolder[] init) {
        List<String> conf = new ArrayList<>();
        for (InitHolder holder : init) {
            conf.add(String.format("%s;%s;%s", holder.attribute().getRegisteredName(), holder.op().getSerializedName(), holder.expression()));
        }
        return conf;
    }

    public List<String> write() {
        return List.copyOf(this.config);
    }

    public static String[] use() {
        return new String[]{
                "Configure attribute increases for the difficulty",
                "Syntax is <attribute>;<operation>;<expression>",
                "Following operations are available: add_value, add_multiplied_base, add_multiplied_total"
        };
    }

    public record InitHolder(Holder<Attribute> attribute, AttributeModifier.Operation op, String expression) {

    }
}
