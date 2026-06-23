package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class WeightedItemStackList {

    public static final Codec<WeightedItemStackList> CODEC = WeightedItemStack.UnresolvedWeightedStack.CODEC.listOf()
            .xmap(WeightedItemStackList::new, l -> {
                List<WeightedItemStack.UnresolvedWeightedStack> sorted = new ArrayList<>(l.values);
                sorted.sort(null);
                return sorted;
            });

    private final List<WeightedItemStack.UnresolvedWeightedStack> values;
    private final List<WeightedItemStack> valid;

    private List<WeightedItemStack> filtered = new ArrayList<>();
    private int totalWeight;
    private double lastModifier = -1;

    public WeightedItemStackList(List<WeightedItemStack.UnresolvedWeightedStack> values) {
        this.values = values;
        this.valid = this.values.stream().flatMap(WeightedItemStack.UnresolvedWeightedStack::resolve).toList();
    }

    public int getTotalWeight(double modifier) {
        if (this.lastModifier != modifier) {
            this.lastModifier = modifier;
            this.calculateTotalWeight(this.lastModifier);
        }
        return this.totalWeight;
    }

    public ItemStack getRandomStack(RandomSource random, double difficulty) {
        if (this.valid.isEmpty())
            return ItemStack.EMPTY;
        int totalWeight = this.getTotalWeight(difficulty);
        if (totalWeight <= 0)
            return ItemStack.EMPTY;
        int index = random.nextInt(totalWeight);
        for (WeightedItemStack entry : this.filtered) {
            index -= entry.getWeight(difficulty);
            if (index < 0) {
                return entry.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    private void calculateTotalWeight(double modifier) {
        this.filtered = this.valid.stream().filter(entry -> entry.getWeight(modifier) > 0).toList();
        this.totalWeight = this.filtered.stream().mapToInt(entry -> entry.getWeight(modifier)).sum();
    }

    @TestOnly
    public JsonElement asProbability(DynamicOps<JsonElement> ops) {
        JsonArray array = new JsonArray();
        float modifier = 250;
        float totalWeight = this.getTotalWeight(modifier);
        for (WeightedItemStack.UnresolvedWeightedStack entry : this.values) {
            array.add(WeightedItemStack.UnresolvedWeightedStack.CODEC.encodeStart(ops, entry.withWeight(totalWeight != 0 ? entry.getWeight(modifier) / totalWeight : 0)).getOrThrow());
        }
        return array;
    }

    @Override
    public String toString() {
        return String.format("TotalWeight: %d ; [%s]", this.totalWeight, this.valid);
    }
}
