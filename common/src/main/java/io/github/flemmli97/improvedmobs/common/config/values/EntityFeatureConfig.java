package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.api.datapack.EntityOverridesManager;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
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
import java.util.Optional;

public class EntityFeatureConfig {

    private final Map<String, EnumSet<DifficultyFeatures>> map = new HashMap<>();

    private final List<String> tagsEntryToResolve = new ArrayList<>();
    private boolean resolved;

    public void initDefault(Level world) {
        this.map.clear();
        for (EntityType<?> entry : BuiltInRegistries.ENTITY_TYPE) {
            try {
                Entity e = entry.create(world);
                if (!(e instanceof Mob))
                    continue;
                EnumSet<DifficultyFeatures> set = EnumSet.noneOf(DifficultyFeatures.class);
                if (set.isEmpty() && !(e instanceof Enemy))
                    set.add(DifficultyFeatures.ALL);
                if (!set.isEmpty())
                    this.map.put(BuiltInRegistries.ENTITY_TYPE.getKey(entry).toString(), set);
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Error during default entity config for EntityType {}, skipping this type. Cause: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entry), e.getMessage());
            }
        }
    }

    public boolean isDisabledFor(Mob living, DifficultyFeatures flag) {
        if (!this.resolved)
            this.resolveTags();
        if (Config.CommonConfig.featureBlacklist.contains(flag))
            return true;
        boolean delegate = EntityOverridesManager.getInstance().isEnabled(living, flag);
        if (delegate)
            return false;
        Optional<ResourceKey<EntityType<?>>> opt = BuiltInRegistries.ENTITY_TYPE.getResourceKey(living.getType());
        if (opt.isEmpty())
            return true;
        ResourceLocation res = opt.get().location();
        boolean reverse = Config.CommonConfig.featureWhitelist.contains(flag);
        EnumSet<DifficultyFeatures> set = this.map.get(res.toString());
        if (set == null)
            set = this.map.get(res.getNamespace());
        if (set != null)
            return reverse ^ set.contains(DifficultyFeatures.REVERSE) ^ (set.contains(DifficultyFeatures.ALL) || set.contains(flag));
        return reverse;
    }

    public void readFromString(List<String> s) {
        this.map.clear();
        for (String val : s) {
            if (val.startsWith("#")) {
                this.tagsEntryToResolve.add(val);
                continue;
            }
            String[] subs = val.split("\\|");
            EnumSet<DifficultyFeatures> set;
            if (subs.length == 1)
                set = EnumSet.of(DifficultyFeatures.ALL);
            else {
                set = EnumSet.noneOf(DifficultyFeatures.class);
                for (int i = 1; i < subs.length; i++)
                    set.add(DifficultyFeatures.valueOf(subs[i].trim()));
            }
            this.map.put(subs[0].trim(), set);
        }
        this.resolved = false;
    }

    public void resolveTags() {
        this.resolved = true;
        for (String val : this.tagsEntryToResolve) {
            String[] subs = val.substring(1).split("\\|");
            EnumSet<DifficultyFeatures> set;
            if (subs.length == 1)
                set = EnumSet.of(DifficultyFeatures.ALL);
            else {
                set = EnumSet.noneOf(DifficultyFeatures.class);
                for (int i = 1; i < subs.length; i++)
                    set.add(DifficultyFeatures.valueOf(subs[i].trim()));
            }
            Iterable<Holder<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.parse(subs[0].trim())));
            tag.forEach(h -> this.map.put(BuiltInRegistries.ENTITY_TYPE.getKey(h.value()).toString(), set));
        }
    }

    public List<String> writeToString() {
        List<String> s = new ArrayList<>();
        for (String key : this.map.keySet()) {
            StringBuilder val = new StringBuilder(key);
            for (DifficultyFeatures f : this.map.get(key)) {
                if (f != DifficultyFeatures.ALL)
                    val.append("|").append(f.name());
            }
            s.add(val.toString());
        }
        return s;
    }

    public static String use() {
        String[] str = new String[]{
                "Entities added here will be blacklisted from their assigned flags. Usage:",
                "<entity registry name> or <namespace> or <#tag> followed by any of:", "[" + Arrays.toString(DifficultyFeatures.values()) + "].",
                "Having no flags is equal to ALL. Use REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
                "Examples (without <>):",
                "<minecraft:sheep> (equal to minecraft:sheep|ALL) excludes sheeps from all modifications",
                "<minecraft:sheep|REVERSE|ATTRIBUTES> add sheep to attributes modification only",
                "<#minecraft:raiders|ATTRIBUTES> will add all entities in the raiders tag to everything except attributes",
                "<minecraft:sheep|ATTRIBUTES> will add sheep to everything except attributes",
                "<minecraft> disables everything for all minecraft mobs"};
        return String.join("\n", str);
    }
}
