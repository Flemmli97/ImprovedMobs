package io.github.flemmli97.improvedmobs.common.config.values;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TridentItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityItemConfig {

    private final List<String> config = new ArrayList<>();
    private boolean initialized;

    private final Map<EntityType<?>, List<String>> itemBlacklist = new HashMap<>();

    @SafeVarargs
    public EntityItemConfig(Pair<String, String>... values) {
        for (Pair<String, String> pair : values) {
            this.config.add(pair.getFirst() + ";" + pair.getSecond());
        }
    }

    public boolean preventUse(Entity entity, Item item) {
        this.initialize();
        List<String> items = this.itemBlacklist.get(entity.getType());
        String remap = this.vanillaRemapping(item);
        return items != null && (items.contains(BuiltInRegistries.ITEM.getKey(item).toString()) || (remap != null && items.contains(remap)));
    }

    private void initialize() {
        if (this.initialized)
            return;
        this.initialized = true;
        Map<EntityType<?>, List<String>> map = new HashMap<>();
        for (String value : this.config) {
            String[] sub = value.split(";");
            if (sub.length != 2) {
                ImprovedMobs.LOGGER.error("Invalid entity item config value for {}", value);
                continue;
            }
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(sub[0]))
                    .ifPresent(type -> map.computeIfAbsent(type, key -> new ArrayList<>()).add(sub[1]));
        }
        this.itemBlacklist.clear();
        this.itemBlacklist.putAll(map);
    }

    public void read(List<String> config) {
        this.config.clear();
        this.config.addAll(config);
        this.itemBlacklist.clear();
        this.initialized = false;
    }

    public List<String> writeToString() {
        return List.copyOf(this.config);
    }

    public static String use() {
        String[] str = new String[]{"<entity registry name;item>",
                "For different items but same entity use multiple lines",
                "Some special names are BOW, TRIDEN, CROSSBOW refering to every bow/trident/crossbow item (So you dont need to type e.g. every bow item)"};
        return String.join("\n", str);
    }

    private String vanillaRemapping(Item item) {
        if (item instanceof BowItem)
            return "BOW";
        if (item instanceof TridentItem)
            return "TRIDENT";
        if (item instanceof CrossbowItem)
            return "CROSSBOW";
        return null;
    }
}
