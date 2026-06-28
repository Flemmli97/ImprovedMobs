package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Allows missing items to be still retained
 */
public record OptionalItemStack(Either<TagKey<Item>, Holder.Reference<Item>> item, int count,
                                DataComponentPatch components) {

    // Doing this roundabout way cause normal HolderSet has verification which we don't want
    public static final Codec<Holder.Reference<Item>> OPTIONAL_HOLDER = ResourceLocation.CODEC
            .xmap(res -> BuiltInRegistries.ITEM.getHolder(res).orElse(Holder.Reference.createStandAlone(BuiltInRegistries.ITEM.holderOwner(), ResourceKey.create(Registries.ITEM, res))),
                    ref -> ref.key().location());

    public static final Codec<Either<TagKey<Item>, Holder.Reference<Item>>> ITEM_HOLDER_CODEC =
            Codec.either(TagKey.hashedCodec(Registries.ITEM), OPTIONAL_HOLDER);

    public static final Codec<OptionalItemStack> CODEC = CodecUtils.tryCodec(ITEM_HOLDER_CODEC
                    .flatXmap(ref -> DataResult.success(new OptionalItemStack(ref)),
                            s -> s.components().isEmpty() && s.count() == 1 ? DataResult.success(s.item()) : DataResult.error(() -> "Not default itemstack")),
            RecordCodecBuilder.create(inst -> inst.group(
                    ITEM_HOLDER_CODEC.fieldOf("id").forGetter(OptionalItemStack::item),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(stack -> stack.count() == 1 ? Optional.empty() : Optional.of(stack.count())),
                    DataComponentPatch.CODEC.optionalFieldOf("components").forGetter((stack) -> stack.components().isEmpty() ? Optional.empty() : Optional.of(stack.components()))
            ).apply(inst, (s, count, comp) -> new OptionalItemStack(s, count.orElse(1), comp.orElse(DataComponentPatch.EMPTY)))));

    public OptionalItemStack(Either<TagKey<Item>, Holder.Reference<Item>> item) {
        this(item, 1, DataComponentPatch.EMPTY);
    }

    public OptionalItemStack(Holder.Reference<Item> item) {
        this(Either.right(item), 1, DataComponentPatch.EMPTY);
    }

    public OptionalItemStack(Holder.Reference<Item> item, Consumer<DataComponentPatch.Builder> components) {
        this(Either.right(item), 1, map(components));
    }

    @SuppressWarnings("deprecation")
    public OptionalItemStack(ItemStack stack) {
        this(Either.right(stack.getItem().builtInRegistryHolder()), stack.getCount(), stack.getComponentsPatch());
    }

    private static DataComponentPatch map(Consumer<DataComponentPatch.Builder> components) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        components.accept(builder);
        return builder.build();
    }

    public Optional<Stream<Holder<Item>>> content() {
        return this.item().map(t -> BuiltInRegistries.ITEM.getTag(t).map(HolderSet.ListBacked::stream), h -> h.isBound() ? Optional.of(Stream.of(h)) : Optional.empty());
    }

    public Key key() {
        return this.item().map(Key::ofTag, h -> Key.ofId(h.key().location()));
    }

    public String keyString() {
        return this.item().map(t -> "#" + t.location(), h -> h.key().location().toString());
    }

    public record Key(String namespace, String path, boolean tag) implements Comparable<Key> {

        public static Key ofTag(TagKey<?> tag) {
            return new Key(tag.location().getNamespace(), tag.location().getPath(), true);
        }

        public static Key ofId(ResourceLocation id) {
            return new Key(id.getNamespace(), id.getPath(), false);
        }

        public String asString() {
            return this.namespace + ":" + this.path;
        }

        @Override
        public int compareTo(Key second) {
            boolean vanilla = this.namespace().equals("minecraft");
            if (vanilla != second.namespace().equals("minecraft")) {
                return vanilla ? -1 : 1;
            }
            int namespace = this.namespace().compareTo(second.namespace());
            if (namespace != 0) {
                return namespace;
            }
            if (this.tag() != second.tag()) {
                return this.tag() ? 1 : -1;
            }
            return this.path().compareTo(second.path());
        }

        @Override
        public String toString() {
            if (this.tag()) {
                return "#" + this.asString();
            }
            return this.asString();
        }
    }
}
