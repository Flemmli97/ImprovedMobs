package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.flemmli97.tenshilib.common.utils.ArrayUtils;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;
import java.util.Map;

public class EntityModifyFlagConfig implements IConfigArrayValue<EntityModifyFlagConfig> {

    private final Map<String, EnumSet<Flags>> map = Maps.newHashMap();

    public EntityModifyFlagConfig() {
        //Init default values as blacklist
        for (EntityType<?> entry : ForgeRegistries.ENTITIES) {
            try {
                //This is as close as it gets since we dont have access to the actual class anymore
                if(entry.getClassification().getPeacefulCreature())
                    this.map.put(entry.getRegistryName().toString(), EnumSet.of(Flags.ALL));
            } catch (Exception e) {
                ImprovedMobs.logger.error("Error with creating entity with null world {}", e);
            }
        }
    }

    public boolean testForFlag(MobEntity living, Flags flag, boolean reverse) {
        ResourceLocation res = living.getType().getRegistryName();
        if (res == null)
            return false;

        EnumSet<Flags> set = this.map.get(res.toString());
        if (set == null)
            set = this.map.get(res.getNamespace());

        if (set != null)
            return reverse ^ set.contains(Flags.REVERSE) ^ (set.contains(Flags.ALL) || set.contains(flag));
        return reverse;
    }

    @Override
    public EntityModifyFlagConfig readFromString(String[] s) {
        this.map.clear();
        for (String val : s) {
            String[] subs = val.split("\\|");

            EnumSet<Flags> set = null;
            if (subs.length == 1)
                set = EnumSet.of(Flags.ALL);
            else {
                set = EnumSet.noneOf(Flags.class);
                for (int i = 1; i < subs.length; i++)
                    set.add(Flags.valueOf(subs[i].trim()));
            }
            this.map.put(subs[0].trim(), set);
        }
        return this;
    }

    @Override
    public String[] writeToString() {
        String[] s = new String[this.map.size()];
        int id = 0;
        for (String key : this.map.keySet()) {
            StringBuilder val = new StringBuilder(key);
            for (Flags f : this.map.get(key)) {
                if (f != Flags.ALL)
                    val.append("|").append(f.name());
            }
            s[id] = val.toString();
            id++;
        }
        return s;
    }

    @Override
    public String usage() {
        String[] str = new String[]{"<entity registry name> followed by any of:", "[" + ArrayUtils.arrayToString(Flags.values()) + "].", "Leave empty to apply them all and REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
                "example: minecraft:sheep|REVERSE|ATTRIBUTES will add sheep to attributes modification (since default is a blacklist)", "or minecraft:sheep|ATTRIBUTES will add sheep to everything except attributes"};
        return String.join("\n", str) + "\n";
    }

    public enum Flags {

        ALL,
        ATTRIBUTES,
        ARMOR,
        HELDITEMS,
        BLOCKBREAK,
        USEITEM,
        LADDER,
        STEAL,
        SWIMMRIDE,
        TARGETVILLAGER,
        //TARGETPLAYER,
        //TARGETHURT,
        REVERSE

    }
}
