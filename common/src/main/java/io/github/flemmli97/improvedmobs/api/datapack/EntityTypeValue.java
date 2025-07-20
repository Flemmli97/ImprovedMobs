package io.github.flemmli97.improvedmobs.api.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class EntityTypeValue {

    public static final Codec<EntityTypeValue> CODEC = new Codec<>() {

        private static final Codec<TagKey<EntityType<?>>> TAG_CODEC = TagKey.hashedCodec(Registries.ENTITY_TYPE);
        private static final Codec<EntityType<?>> TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
        private static final Codec<String> NAME_SPACE_CODEC = Codec.STRING.validate(s -> ResourceLocation.isValidNamespace(s)
                ? DataResult.success(s)
                : DataResult.error(() -> "Invalid namespace string " + s));

        @Override
        public <T> DataResult<Pair<EntityTypeValue, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<TagKey<EntityType<?>>, T>> res = TAG_CODEC.decode(ops, input);
            if (res.isSuccess())
                return res.map(v -> v.mapFirst(EntityTypeValue::ofTag));
            DataResult<Pair<EntityType<?>, T>> res2 = TYPE_CODEC.decode(ops, input);
            if (res2.isSuccess())
                return res2.map(v -> v.mapFirst(EntityTypeValue::ofType));
            DataResult<Pair<String, T>> res3 = NAME_SPACE_CODEC.decode(ops, input);
            if (res3.isSuccess())
                return res3.map(v -> v.mapFirst(EntityTypeValue::ofNamespace));
            return DataResult.error(() -> "Unable to decode input " + input);
        }

        @Override
        public <T> DataResult<T> encode(EntityTypeValue input, DynamicOps<T> ops, T prefix) {
            if (input.tag != null)
                return TAG_CODEC.encode(input.tag, ops, prefix);
            if (input.type != null)
                return TYPE_CODEC.encode(input.type, ops, prefix);
            if (input.modid != null)
                return NAME_SPACE_CODEC.encode(input.modid, ops, prefix);
            return DataResult.error(() -> "Unable to encode value " + input);
        }
    };

    @Nullable
    private final TagKey<EntityType<?>> tag;
    @Nullable
    private final EntityType<?> type;
    @Nullable
    private final String modid;

    private EntityTypeValue(TagKey<EntityType<?>> tag, EntityType<?> type, String modid) {
        this.tag = tag;
        this.type = type;
        this.modid = modid;
    }

    public static EntityTypeValue ofTag(TagKey<EntityType<?>> tag) {
        return new EntityTypeValue(tag, null, null);
    }

    public static EntityTypeValue ofType(EntityType<?> type) {
        return new EntityTypeValue(null, type, null);
    }

    public static EntityTypeValue ofNamespace(String modid) {
        return new EntityTypeValue(null, null, modid);
    }

    public boolean isTag(Holder<EntityType<?>> type) {
        return this.tag != null && type.is(this.tag);
    }

    public boolean isDirect(Holder<EntityType<?>> type) {
        return this.type != null && this.type == type.value();
    }

    public boolean isNamespace(Holder<EntityType<?>> type) {
        return type.unwrapKey().orElseThrow().location().getNamespace().equals(this.modid);
    }
}
