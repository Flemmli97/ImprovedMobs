package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.TestOnly;

import java.util.Optional;
import java.util.stream.Stream;

public class WeightedItemStack {

    private final UnresolvedWeightedStack data;
    private final ItemStack stack;
    private final double weight;
    private final double quality;

    private WeightedItemStack(UnresolvedWeightedStack data, ItemStack stack, double itemWeight, double quality) {
        this.data = data;
        this.stack = stack;
        this.weight = itemWeight;
        this.quality = quality;
    }

    public ItemStack getItem() {
        return this.stack.copy();
    }

    public int getWeight(double modifier) {
        return this.data.getWeight(modifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof WeightedItemStack other) {
            return ItemStack.matches(this.stack, other.stack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Item: %s; Weight: %s, Quality: %s", this.data.item().keyString(), this.weight, this.quality);
    }

    public record UnresolvedWeightedStack(OptionalItemStack item, double weight,
                                          double quality) implements Comparable<UnresolvedWeightedStack> {

        public static final Codec<UnresolvedWeightedStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                OptionalItemStack.CODEC.fieldOf("value").forGetter(UnresolvedWeightedStack::item),
                Codec.DOUBLE.fieldOf("weight").forGetter(UnresolvedWeightedStack::weight),
                Codec.DOUBLE.fieldOf("quality").forGetter(UnresolvedWeightedStack::quality)
        ).apply(inst, UnresolvedWeightedStack::new));

        public Stream<WeightedItemStack> resolve() {
            if (!this.valid())
                return Stream.empty();
            Optional<Stream<Holder<Item>>> content = this.item.content();
            if (content.isEmpty()) {
                ImprovedMobs.LOGGER.error("equipment.json: Entry {} is missing", this.item.keyString());
                return Stream.empty();
            }
            return content.get().map(h -> new WeightedItemStack(this, new ItemStack(h, this.item().count(), this.item().components()), this.weight, this.quality));
        }

        public boolean valid() {
            return (this.weight() > 0 || this.quality() > 0) && this.isEnabled();
        }

        public boolean isEnabled() {
            if (Config.CommonConfig.equipmentModWhitelist) {
                return Config.CommonConfig.equipmentModBlacklist.contains(this.item().key().namespace());
            }
            return !Config.CommonConfig.equipmentModBlacklist.contains(this.item().key().namespace());
        }

        public int getWeight(double modifier) {
            return (int) Math.ceil(Math.max(this.weight + modifier * this.quality, 0));
        }

        @TestOnly
        public UnresolvedWeightedStack withWeight(float weight) {
            return new UnresolvedWeightedStack(this.item, weight, this.quality);
        }

        @Override
        public int compareTo(UnresolvedWeightedStack o) {
            return this.item().key().compareTo(o.item().key());
        }
    }
}
