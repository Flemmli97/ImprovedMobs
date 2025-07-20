package io.github.flemmli97.improvedmobs.api;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ImprovedMobsTags {

    public static final TagKey<Block> NON_STEALABLE_BLOCK = TagKey.create(Registries.BLOCK, ImprovedMobs.modRes("non_steal"));
}
