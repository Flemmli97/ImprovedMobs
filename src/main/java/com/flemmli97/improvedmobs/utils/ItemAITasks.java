package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.config.Config;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.Hand;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ItemAITasks {

    private static final Map<Item, ItemAI> itemMap = new HashMap<>();

    public static void initAI() {
        initVanilla();
    }

    /**
     * Register during FMLCommonSetupEvent. Not Thread safe.
     */
    public static void registerAI(Item item, ItemAI ai) {
        itemMap.put(item, ai);
    }

    @Nullable
    public static ItemAI getAI(Item item) {
        return itemMap.get(item);
    }

    @Nullable
    public static Pair<ItemAI, Hand> getAI(MobEntity entity) {
        ItemStack heldMain = entity.getHeldItemMainhand();
        ItemStack heldOff = entity.getHeldItemOffhand();
        if (heldMain.getItem() instanceof ArrowItem && heldOff.getItem() instanceof BowItem) {
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, heldOff.copy());
            entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, heldMain.copy());
            heldMain = entity.getHeldItemMainhand();
            heldOff = entity.getHeldItemOffhand();
        }
        Hand hand = Hand.MAIN_HAND;
        ItemAI ai = itemMap.get(heldMain.getItem());
        if (ai == null || ai.prefHand() == ItemAI.UsableHand.OFF || blockedAI(entity, heldMain.getItem()) || !ai.applies(heldMain)) {
            ai = itemMap.get(heldOff.getItem());
            if (ai != null) {
                if (ai.prefHand() == ItemAI.UsableHand.MAIN || blockedAI(entity, heldOff.getItem()) || !ai.applies(heldOff))
                    ai = null;
                else hand = Hand.OFF_HAND;
            }
        }
        return Pair.of(ai, hand);
    }

    private static boolean blockedAI(MobEntity entity, Item item) {
        return (Config.CommonConfig.mobListUseWhitelist && !Config.CommonConfig.itemuseBlacklist.contains(item.getRegistryName().toString()))
                || Config.CommonConfig.itemuseBlacklist.contains(item.getRegistryName().toString())
                || Config.CommonConfig.entityItemConfig.preventUse(entity, item);
    }

    private static void initVanilla() {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof SplashPotionItem)
                registerAI(item, ItemAIs.SPLASH);
            if (item instanceof LingeringPotionItem)
                registerAI(item, ItemAIs.LINGERINGPOTIONS);
            if (item instanceof CrossbowItem)
                registerAI(item, ItemAIs.CROSSBOWS);
            if (item instanceof BowItem)
                registerAI(item, ItemAIs.BOWS);
            if (item instanceof ShieldItem)
                registerAI(item, ItemAIs.SHIELDS);
        }

        registerAI(Items.SNOWBALL, ItemAIs.SNOWBALL);
        registerAI(Items.ENDER_PEARL, ItemAIs.ENDER_PEARL);
        registerAI(Items.LAVA_BUCKET, ItemAIs.LAVABUCKET);
        registerAI(Items.FLINT_AND_STEEL, ItemAIs.FLINT_N_STEEL);
        registerAI(Blocks.TNT.asItem(), ItemAIs.TNT);
        registerAI(Items.TRIDENT, ItemAIs.TRIDENT);
        registerAI(Items.ENCHANTED_BOOK, ItemAIs.ENCHANTEDBOOK);
    }
}
