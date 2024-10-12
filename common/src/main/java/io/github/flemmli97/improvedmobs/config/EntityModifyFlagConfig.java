package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

public class EntityModifyFlagConfig {

    private final Map<String, EnumSet<Flags>> map = new HashMap<>();

    private final List<String> tagsEntryToResolve = new ArrayList<>();
    private boolean resolved;

    @SuppressWarnings("deprecation")
    public void initDefault(Level world) {
        this.map.clear();
        for (EntityType<?> entry : BuiltInRegistries.ENTITY_TYPE) {
            try {
                Entity e = entry.create(world);
                if (!(e instanceof Mob))
                    continue;
                EnumSet<Flags> set = EnumSet.noneOf(Flags.class);
                for (Flags flag : Flags.values()) {
                    if (flag.tag == null)
                        continue;
                    if (entry.builtInRegistryHolder().is(flag.tag))
                        set.add(flag);
                }
                if (set.isEmpty() && !(e instanceof Enemy))
                    set.add(Flags.ALL);
                if (!set.isEmpty())
                    this.map.put(BuiltInRegistries.ENTITY_TYPE.getKey(entry).toString(), set);
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Error during default entity config for EntityType {}, skipping this type. Cause: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entry), e.getMessage());
            }
        }
    }

    public boolean hasFlag(Mob living, Flags flag, boolean reverse) {
        if (!this.resolved)
            this.resolveTags();
        ResourceLocation res = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
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

    public EntityModifyFlagConfig readFromString(List<String> s) {
        this.map.clear();
        for (String val : s) {
            if (val.startsWith("#")) {
                this.tagsEntryToResolve.add(val);
                continue;
            }
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

    public void resolveTags() {
        this.resolved = true;
        for (String val : this.tagsEntryToResolve) {
            String[] subs = val.substring(1).split("\\|");
            EnumSet<Flags> set;
            if (subs.length == 1)
                set = EnumSet.of(Flags.ALL);
            else {
                set = EnumSet.noneOf(Flags.class);
                for (int i = 1; i < subs.length; i++)
                    set.add(Flags.valueOf(subs[i].trim()));
            }
            Iterable<Holder<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.parse(subs[0].trim())));
            tag.forEach(h -> this.map.put(BuiltInRegistries.ENTITY_TYPE.getKey(h.value()).toString(), set));
        }
    }

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
        String[] str = new String[]{"Entities added here will be blacklisted from their assigned flags. Usage:", "<entity registry name> or <namespace> or <#tag> followed by any of:", "[" + ArrayUtils.arrayToString(Flags.values()) + "].", "Having no flags is equal to ALL. Use REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
                "Examples (without <>):", "<minecraft:sheep> (equal to minecraft:sheep|ALL) excludes sheeps from all modifications", "<minecraft:sheep|REVERSE|ATTRIBUTES will> add sheep to attributes modification only",
                "<#minecraft:raiders|ATTRIBUTES> will add all entities in the raiders tag to everything except attributes", "<minecraft:sheep|ATTRIBUTES> will add sheep to everything except attributes", "<minecraft> disables everything for all minecraft mobs"};
        return String.join("\n", str);
    }

    public enum Flags {

        ALL(null),
        ATTRIBUTES("attributes"),
        ARMOR("armor"),
        HELDITEMS("helditems"),
        BLOCKBREAK("blockbreak"),
        USEITEM("useitem"),
        LADDER("ladder"),
        STEAL("steal"),
        GUARDIAN("guardian"),
        PARROT("parrot"),
        TARGETVILLAGER("villager"),
        NEUTRALAGGRO("neutral"),
        PEHKUI("pehkui"),
        REVERSE(null);

        /**
         * Used in initializing the default list. Here for easier tweaking of the default list by other mods.
         * Not for endusers
         */
        public final TagKey<EntityType<?>> tag;

        Flags(String id) {
            if (id == null)
                this.tag = null;
            else
                this.tag = TagKey.create(Registries.ENTITY_TYPE, ImprovedMobs.modRes("default_blacklist_" + id));
        }

        public static List<Flags> toggable() {
            List<Flags> all = new ArrayList<>(Arrays.asList(Flags.values()));
            all.remove(Flags.ALL);
            all.remove(Flags.REVERSE);
            return all;
        }
    }
}
