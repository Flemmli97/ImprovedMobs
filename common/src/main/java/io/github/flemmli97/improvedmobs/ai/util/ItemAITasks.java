package io.github.flemmli97.improvedmobs.ai.util;

import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemAITasks {

    private static final Map<Item, ItemAI> ITEM_MAP = new HashMap<>();

    public static void initAI() {
        initVanilla();
    }

    /**
     * Register during FMLCommonSetupEvent. Not Thread safe.
     */
    public static void registerAI(Item item, ItemAI ai) {
        ITEM_MAP.put(item, ai);
    }

    @Nullable
    public static ItemAI getAI(Item item) {
        return ITEM_MAP.get(item);
    }

    @Nullable
    public static Pair<ItemAI, InteractionHand> getAI(Mob entity) {
        ItemStack heldMain = entity.getMainHandItem();
        ItemStack heldOff = entity.getOffhandItem();
        if (heldMain.getItem() instanceof ArrowItem && heldOff.getItem() instanceof BowItem) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, heldOff.copy());
            entity.setItemSlot(EquipmentSlot.OFFHAND, heldMain.copy());
            heldMain = entity.getMainHandItem();
            heldOff = entity.getOffhandItem();
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemAI ai = ITEM_MAP.get(heldMain.getItem());
        if (ai == null || ai.prefHand() == ItemAI.UsableHand.OFF || blockedAI(entity, heldMain.getItem()) || !ai.applies(heldMain)) {
            ai = ITEM_MAP.get(heldOff.getItem());
            if (ai != null) {
                if (ai.prefHand() == ItemAI.UsableHand.MAIN || ai.isIncompatibleWith(entity, heldMain) || blockedAI(entity, heldOff.getItem()) || !ai.applies(heldOff))
                    ai = null;
                else hand = InteractionHand.OFF_HAND;
            }
        }
        return Pair.of(ai, hand);
    }

    private static boolean blockedAI(Mob entity, Item item) {
        return (Config.CommonConfig.itemuseWhitelist && !Config.CommonConfig.itemuseBlacklist.contains(BuiltInRegistries.ITEM.getKey(item).toString()))
                || Config.CommonConfig.itemuseBlacklist.contains(BuiltInRegistries.ITEM.getKey(item).toString())
                || Config.CommonConfig.entityItemConfig.preventUse(entity, item);
    }

    private static void initVanilla() {
        for (Item item : BuiltInRegistries.ITEM) {
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
