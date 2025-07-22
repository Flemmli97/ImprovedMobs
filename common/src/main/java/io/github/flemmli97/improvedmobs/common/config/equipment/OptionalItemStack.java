package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Allows missing items to be still retained
 */
public record OptionalItemStack(Holder.Reference<Item> item, int count, DataComponentPatch components) {

    public static final Codec<Holder.Reference<Item>> OPTIONAL_HOLDER = ResourceLocation.CODEC
            .xmap(res -> BuiltInRegistries.ITEM.getHolder(res).orElse(Holder.Reference.createStandAlone(BuiltInRegistries.ITEM.holderOwner(), ResourceKey.create(Registries.ITEM, res))),
                    ref -> ref.key().location());

    public static final Codec<OptionalItemStack> CODEC = CodecUtils.tryCodec(OPTIONAL_HOLDER
                    .flatXmap(ref -> DataResult.success(new OptionalItemStack(ref)),
                            s -> s.components().isEmpty() && s.count() == 1 ? DataResult.success(s.item()) : DataResult.error(() -> "Not default itemstack")),
            RecordCodecBuilder.create(inst -> inst.group(
                    OPTIONAL_HOLDER.fieldOf("id").forGetter(OptionalItemStack::item),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(stack -> stack.count() == 1 ? Optional.empty() : Optional.of(stack.count())),
                    DataComponentPatch.CODEC.optionalFieldOf("components").forGetter((stack) -> stack.components().isEmpty() ? Optional.empty() : Optional.of(stack.components()))
            ).apply(inst, (s, count, comp) -> new OptionalItemStack(s, count.orElse(1), comp.orElse(DataComponentPatch.EMPTY)))));

    public OptionalItemStack(Holder.Reference<Item> item) {
        this(item, 1, DataComponentPatch.EMPTY);
    }

    public OptionalItemStack(Holder.Reference<Item> item, Consumer<DataComponentPatch.Builder> components) {
        this(item, 1, map(components));
    }

    private static DataComponentPatch map(Consumer<DataComponentPatch.Builder> components) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        components.accept(builder);
        return builder.build();
    }

    public ItemStack asStack() {
        if (this.item().isBound()) {
            return new ItemStack(this.item(), this.count(), this.components());
        }
        return ItemStack.EMPTY;
    }
}
