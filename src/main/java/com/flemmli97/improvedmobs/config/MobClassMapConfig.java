package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigListValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class MobClassMapConfig implements IConfigListValue<MobClassMapConfig> {

    private final Map<ResourceLocation, List<EntityType<?>>> map = Maps.newLinkedHashMap();
    public Map<ResourceLocation, Predicate<Class<? extends MobEntity>>> preds = new HashMap<>();

    @Nullable
    public List<EntityType<?>> get(ResourceLocation res) {
        return this.map.get(res);
    }

    @Override
    public MobClassMapConfig readFromString(List<String> ss) {
        this.map.clear();
        for (String s : ss) {
            String[] sub = s.replace(" ", "").split("-");
            if (sub.length < 2)
                continue;
            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(sub[1]));
            if (type == null) {
                ImprovedMobs.logger.error("Entity {} does not exist/is not registered", sub[1]);
                continue;
            }
            this.map.merge(new ResourceLocation(sub[0]), Lists.newArrayList(type), (old, oth) -> {
                old.add(type);
                return old;
            });
        }
        return this;
    }

    @Override
    public List<String> writeToString() {
        List<String> l = new ArrayList<>();
        for (Entry<ResourceLocation, List<EntityType<?>>> ent : this.map.entrySet()) {
            for (EntityType<?> type : ent.getValue()) {
                l.add(ent.getKey().toString() + "-" + type.getRegistryName());
            }
        }
        return l;
    }

    public static String use() {
        return "[mob id]-[mob id] where second value is the target.\n e.g. minecraft:zombie-minecraft:skeleton makes all zombies target skeletons";
    }
}
