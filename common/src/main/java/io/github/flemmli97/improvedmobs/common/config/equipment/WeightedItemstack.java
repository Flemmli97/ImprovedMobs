package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.TestOnly;

public class WeightedItemstack implements Comparable<WeightedItemstack> {

    public static final Codec<WeightedItemstack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            OptionalItemStack.CODEC.fieldOf("value").forGetter(d -> d.item),
            Codec.FLOAT.fieldOf("weight").forGetter(d -> d.weight),
            Codec.FLOAT.fieldOf("quality").forGetter(d -> d.quality)
    ).apply(inst, WeightedItemstack::new));

    private final OptionalItemStack item;
    private final ItemStack stack;
    private final float weight;
    private final float quality;

    public WeightedItemstack(OptionalItemStack item, float itemWeight, float quality) {
        this.item = item;
        this.stack = item.asStack();
        this.weight = itemWeight;
        this.quality = quality;
    }

    public ItemStack getItem() {
        return this.stack.copy();
    }

    public float weight() {
        return this.weight;
    }

    public float quality() {
        return this.quality;
    }

    public int getWeight(double modifier) {
        return (int) Math.max(this.weight + Mth.floor(modifier * this.quality), 0);
    }

    public boolean valid() {
        return !this.stack.isEmpty() && (this.weight() > 0 || this.quality() > 0) && this.isEnabled();
    }

    public boolean isEnabled() {
        if (Config.CommonConfig.equipmentModWhitelist) {
            return Config.CommonConfig.equipmentModBlacklist.contains(this.item.item().key().location().getNamespace());
        }
        return !Config.CommonConfig.equipmentModBlacklist.contains(this.item.item().key().location().getNamespace());
    }

    @TestOnly
    public WeightedItemstack withWeight(float weight) {
        return new WeightedItemstack(this.item, weight, this.quality);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof WeightedItemstack other) {
            return ItemStack.matches(this.stack, other.stack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.item.hashCode();
    }

    @Override
    public int compareTo(WeightedItemstack o) {
        return this.item.item().getRegisteredName().compareTo(o.item.item().getRegisteredName());
    }

    @Override
    public String toString() {
        return String.format("Item: %s; Weight: %s, Quality: %s", this.item.item().getRegisteredName(), this.weight, this.quality);
    }
}
