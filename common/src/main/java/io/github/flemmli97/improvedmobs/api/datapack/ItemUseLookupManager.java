package io.github.flemmli97.improvedmobs.api.datapack;

import com.google.common.collect.ImmutableMap;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.ItemUseRegistry;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.equipment.EquipmentList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ItemUseLookupManager implements PreparableReloadListener {

    public static final ResourceLocation ID = ImprovedMobs.modRes("item_use_lookup");

    private static ItemUseLookupManager INSTANCE;

    private Map<Item, Collection<ItemUseHandler>> lookup = ImmutableMap.of();

    private final HolderLookup.Provider provider;

    private ItemUseLookupManager(HolderLookup.Provider provider) {
        this.provider = provider;
    }

    public static ItemUseLookupManager create(HolderLookup.Provider provider) {
        ItemUseLookupManager.INSTANCE = new ItemUseLookupManager(provider);
        return getInstance();
    }

    public static ItemUseLookupManager getInstance() {
        return INSTANCE;
    }

    public Collection<ItemUseHandler> get(Item item) {
        return this.lookup.getOrDefault(item, Collections.emptyList());
    }

    public Pair<ItemUseHandler, InteractionHand> get(Mob entity) {
        ItemStack heldMain = entity.getMainHandItem();
        ItemStack heldOff = entity.getOffhandItem();
        if (heldMain.getItem() instanceof ArrowItem && heldOff.getItem() instanceof BowItem) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, heldOff.copy());
            entity.setItemSlot(EquipmentSlot.OFFHAND, heldMain.copy());
            heldMain = entity.getMainHandItem();
            heldOff = entity.getOffhandItem();
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack mainCheck = heldMain;
        ItemUseHandler ai = this.get(heldMain.getItem()).stream().filter(handler -> handler.canUse(entity, mainCheck, false))
                .findFirst().orElse(null);
        if (ai == null || ai.preferredHand() == ItemUseHandler.PreferredHand.OFFHAND || blockedAI(entity, heldMain.getItem())) {
            ItemStack offCheck = heldOff;
            ai = this.get(heldOff.getItem()).stream().filter(handler -> handler.canUse(entity, offCheck, false))
                    .findFirst().orElse(null);
            if (ai != null) {
                if (ai.preferredHand() == ItemUseHandler.PreferredHand.MAINHAND || blockedAI(entity, heldOff.getItem()))
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

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
            ImmutableMap.Builder<Item, Collection<ItemUseHandler>> builder = ImmutableMap.builder();
            for (Item item : BuiltInRegistries.ITEM) {
                List<ItemUseHandler> handlers = ItemUseRegistry.getAll().stream().filter(handler -> handler.matches(item)).toList();
                if (!handlers.isEmpty())
                    builder.put(item, handlers);
            }
            this.lookup = builder.build();
        }).thenRun(() -> EquipmentList.initEquip(this.provider, this)).thenCompose(stage::wait);
    }
}
