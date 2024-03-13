package io.github.flemmli97.improvedmobs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final Logger logger = LogManager.getLogger(ImprovedMobs.MODID);

    public static TagKey<EntityType<?>> ARMOR_EQUIPPABLE = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(MODID, "armor_equippable"));
    public static TagKey<Block> SEE_THROUGH = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "see_through"));
}
