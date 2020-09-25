package com.flemmli97.improvedmobs.config;

import com.flemmli97.tenshilib.api.config.IConfigListValue;
import com.flemmli97.tenshilib.common.utils.ArrayUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class EntityModifyFlagConfig implements IConfigListValue<EntityModifyFlagConfig> {

    private final Map<String, EnumSet<Flags>> map = Maps.newHashMap();

    public void initDefault(World world){
        this.map.clear();
        for (EntityType<?> entry : ForgeRegistries.ENTITIES) {
            Entity e = entry.create(world);
            if(!(e instanceof MobEntity) || e instanceof MonsterEntity || e instanceof GhastEntity || e instanceof PhantomEntity || e instanceof SlimeEntity || e instanceof ShulkerEntity || e instanceof TameableEntity)
                continue;
            this.map.put(entry.getRegistryName().toString(), EnumSet.of(Flags.ALL));
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
    public EntityModifyFlagConfig readFromString(List<String> s) {
        this.map.clear();
        for (String val : s) {
            String[] subs = val.split("\\|");
            EnumSet<Flags> set;
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
    public List<String> writeToString() {
        List<String> s = Lists.newArrayList();
        for (String key : this.map.keySet()) {
            StringBuilder val = new StringBuilder(key);
            for (Flags f : this.map.get(key)) {
                if (f != Flags.ALL)
                    val.append("|").append(f.name());
            }
            s.add(val.toString());
        }
        return s;
    }

    public static String use(){
        String[] str = new String[]{"<entity registry name> followed by any of:", "[" + ArrayUtils.arrayToString(Flags.values()) + "].", "Leave empty to apply them all and REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
                "example: minecraft:sheep|REVERSE|ATTRIBUTES will add sheep to attributes modification (since default is a blacklist)", "or minecraft:sheep|ATTRIBUTES will add sheep to everything except attributes"};
        return String.join("\n", str);
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
