package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.api.datapack.EntityOverridesManager;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class EntityFeatureConfig {

    private final List<String> config = new ArrayList<>();
    private boolean initialized;

    private final Map<EntityType<?>, EnumSet<DifficultyFeatures>> features = new HashMap<>();

    public void initDefault(Level level) {
        this.features.clear();
        BuiltInRegistries.ENTITY_TYPE.holders().forEach(type -> {
            try {
                Entity entity = type.value().create(level);
                if (entity instanceof Mob && !(entity instanceof Enemy))
                    this.config.add(type.getRegisteredName());
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Error during default entity config for EntityType {}, skipping this type. Cause: {}", type.key(), e.getMessage());
            }
        });
    }

    public boolean isDisabledFor(Mob living, DifficultyFeatures flag) {
        if (Config.CommonConfig.featureBlacklist.contains(flag))
            return true;
        this.initialize();
        EntityOverridesManager.OverrideState override = EntityOverridesManager.getInstance().isEnabled(living, flag);
        if (override == EntityOverridesManager.OverrideState.ON)
            return false;
        if (override == EntityOverridesManager.OverrideState.OFF)
            return true;
        boolean reverse = Config.CommonConfig.featureWhitelist.contains(flag);
        EnumSet<DifficultyFeatures> set = this.features.get(living.getType());
        if (set != null)
            return reverse ^ set.contains(DifficultyFeatures.REVERSE) ^ (set.contains(DifficultyFeatures.ALL) || set.contains(flag));
        return reverse;
    }

    private void initialize() {
        if (this.initialized)
            return;
        this.initialized = true;
        Map<EntityType<?>, EnumSet<DifficultyFeatures>> direct = new HashMap<>();
        Map<EntityType<?>, EnumSet<DifficultyFeatures>> tags = new HashMap<>();
        Map<EntityType<?>, EnumSet<DifficultyFeatures>> namespace = new HashMap<>();
        BuiltInRegistries.ENTITY_TYPE.holders().forEach(type -> {
            this.config.forEach(val -> {
                String[] subs = val.replace(" ", "").split("\\|");
                EnumSet<DifficultyFeatures> set;
                if (subs.length == 1)
                    set = EnumSet.of(DifficultyFeatures.ALL);
                else {
                    set = EnumSet.noneOf(DifficultyFeatures.class);
                    for (int i = 1; i < subs.length; i++)
                        set.add(DifficultyFeatures.valueOf(subs[i]));
                }
                String entity = subs[0];
                if (entity.startsWith("#")) {
                    if (type.is(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.parse(entity.substring(1)))))
                        tags.put(type.value(), set);
                } else if (type.getRegisteredName().equals(entity)) {
                    direct.put(type.value(), set);
                } else if (type.key().location().getNamespace().equals(entity)) {
                    namespace.put(type.value(), set);
                }
            });
        });
        tags.forEach((type, features) -> {
            if (!direct.containsKey(type))
                direct.put(type, features);
        });
        namespace.forEach((type, features) -> {
            if (!direct.containsKey(type))
                direct.put(type, features);
        });
        this.features.putAll(direct);
    }

    public void read(List<String> config) {
        this.config.clear();
        this.config.addAll(config);
        this.features.clear();
        this.initialized = false;
    }

    public List<String> write() {
        return List.copyOf(this.config);
    }

    public static String use() {
        String[] str = new String[]{
                "Mods can override this now with a datapack! Check for the message 'Following entity overrides are loaded' in your logs to see any overrides",
                "Entities added here will be blacklisted from that feature. Usage:",
                "<entity registry name | namespace | #tag> followed by any of:", "[" + Arrays.toString(DifficultyFeatures.values()) + "].",
                "Having nothing is equal to ALL. Use REVERSE to reverse all features. Some features do nothing for certain mobs!",
                "Examples (without <>):",
                "<minecraft:sheep> (equal to minecraft:sheep|ALL) excludes sheeps from all modifications",
                "<minecraft:sheep|REVERSE|ATTRIBUTES> add sheep to attributes modification only",
                "<#minecraft:raiders|ATTRIBUTES> will make all raiders be excluded from attribute modifications",
                "<minecraft:sheep|ATTRIBUTES> will make sheeps be excluded from attribute modifications",
                "<minecraft> disables everything for all vanilla entities"};
        return String.join("\n", str);
    }
}
