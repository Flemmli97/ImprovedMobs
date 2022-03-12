package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.api.config.IConfigListValue;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityModifyFlagConfig implements IConfigListValue<EntityModifyFlagConfig> {

    private final Map<String, EnumSet<Flags>> map = new HashMap<>();

    public void initDefault(Level world) {
        this.map.clear();
        for (EntityType<?> entry : PlatformUtils.INSTANCE.entities().getIterator()) {
            try {
                Entity e = entry.create(world);
                if (!(e instanceof Mob) || e instanceof Enemy)
                    continue;
                this.map.put(PlatformUtils.INSTANCE.entities().getIDFrom(entry).toString(), EnumSet.of(Flags.ALL));
            } catch (Exception e) {
                ImprovedMobs.logger.error("Error during default entity config for EntityType {}, skipping this type. Cause: {}", PlatformUtils.INSTANCE.entities().getIDFrom(entry), e.getMessage());
            }
        }
    }

    public boolean hasFlag(Mob living, Flags flag, boolean reverse) {
        ResourceLocation res = PlatformUtils.INSTANCE.entities().getIDFrom(living.getType());
        if (res == null)
            return true;
        if (Config.CommonConfig.flagBlacklist.contains(flag.toString()))
            return true;
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
        List<String> s = new ArrayList<>();
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

    public static String use() {
        String[] str = new String[]{"Entities added here will be blacklisted from their assigned flags. Usage:", "<entity registry name> or <namespace> followed by any of:", "[" + ArrayUtils.arrayToString(Flags.values()) + "].", "Having no flags is equal to ALL. Use REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
                "Examples:", "minecraft:sheep is equal to minecraft:sheep|ALL and excludes sheeps from all modifications", "minecraft:sheep|REVERSE|ATTRIBUTES will add sheep to attributes modification only", "minecraft:sheep|ATTRIBUTES will add sheep to everything except attributes"};
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
        GUARDIAN,
        PARROT,
        TARGETVILLAGER,
        NEUTRALAGGRO,
        REVERSE;

        public static List<Flags> toggable() {
            List<Flags> all = new ArrayList<>(Arrays.asList(Flags.values()));
            all.remove(Flags.ALL);
            all.remove(Flags.REVERSE);
            return all;
        }
    }
}