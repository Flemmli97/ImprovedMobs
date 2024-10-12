package io.github.flemmli97.improvedmobs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final Logger LOGGER = LogManager.getLogger(ImprovedMobs.MODID);

    public static TagKey<EntityType<?>> ARMOR_EQUIPPABLE = TagKey.create(Registries.ENTITY_TYPE, modRes("armor_equippable"));
    public static TagKey<Block> SEE_THROUGH = TagKey.create(Registries.BLOCK, modRes("see_through"));

    public static ResourceLocation modRes(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
