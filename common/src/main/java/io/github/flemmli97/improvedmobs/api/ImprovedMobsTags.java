package io.github.flemmli97.improvedmobs.api;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;

public class ImprovedMobsTags {

    public static final TagKey<Block> NON_STEALABLE_BLOCK = TagKey.create(Registries.BLOCK, ImprovedMobs.modRes("non_steal"));

    public static final TagKey<MobEffect> HARMFUL_EFFECT = TagKey.create(Registries.MOB_EFFECT, ImprovedMobs.modRes("harmful_effect"));
    public static final TagKey<MobEffect> ENCHANTED_BOOK_EFFECT = TagKey.create(Registries.MOB_EFFECT, ImprovedMobs.modRes("enchanted_book_effect"));
}
