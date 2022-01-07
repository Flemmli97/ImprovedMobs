package io.github.flemmli97.improvedmobs.config;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.RegistryHelper;
import io.github.flemmli97.tenshilib.api.config.IConfigListValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TridentItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityItemConfig implements IConfigListValue<EntityItemConfig> {

    private final Map<ResourceLocation, List<String>> itemBlacklist = new HashMap<>();

    public EntityItemConfig add(ResourceLocation res, String item) {
        this.itemBlacklist.merge(res, Lists.newArrayList(item), (o, n) -> {
            o.add(item);
            return o;
        });
        return this;
    }

    public boolean preventUse(Entity entity, Item item) {
        List<String> items = this.itemBlacklist.get(RegistryHelper.entities().getIDFrom(entity.getType()));
        String remap = this.vanillaRemapping(item);
        return items != null && (items.contains(RegistryHelper.items().getIDFrom(item).toString()) || (remap != null && items.contains(remap)));
    }

    @Override
    public EntityItemConfig readFromString(List<String> list) {
        Map<ResourceLocation, List<String>> temp = new HashMap<>();
        list.forEach(s -> {
            String[] sub = s.split(";");
            if (sub.length == 2) {
                temp.merge(new ResourceLocation(sub[0]), Lists.newArrayList(sub[1]), (o, n) -> {
                    o.add(sub[1]);
                    return o;
                });
            } else
                ImprovedMobs.logger.error("Invalid entity item config value for {}", s);
        });
        this.itemBlacklist.clear();
        this.itemBlacklist.putAll(temp);
        return this;
    }

    @Override
    public List<String> writeToString() {
        List<String> list = new ArrayList<>();
        this.itemBlacklist.forEach((res, il) -> il.forEach(s -> list.add(res.toString() + ";" + s)));
        list.sort(null);
        return list;
    }

    public static String use() {
        String[] str = new String[]{"<entity registry name-item>", "For different items but same entity use multiple lines",
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
