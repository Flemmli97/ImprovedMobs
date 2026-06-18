package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.datapack.ItemUseLookupManager;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;
import io.github.flemmli97.tenshilib.common.utils.ItemUtils;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentList {

    private static final int CONFIG_VERSION = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Codec<Map<EquipmentSlot, WeightedItemstackList>> CODEC = Codec.unboundedMap(CodecUtils.stringEnumCodec(EquipmentSlot.class, null),
            WeightedItemstackList.CODEC).xmap(EnumMap::new, m -> m);

    private static Map<EquipmentSlot, WeightedItemstackList> EQUIPMENTS = new EnumMap<>(EquipmentSlot.class);

    public static ItemStack getEquipment(Mob mob, EquipmentSlot slot, double difficulty) {
        WeightedItemstackList eq = EQUIPMENTS.get(slot);
        if (eq == null)
            return ItemStack.EMPTY;
        return eq.getRandomStack(mob.getRandom(), difficulty);
    }

    public static void initEquip(HolderLookup.Provider provider, ItemUseLookupManager manager) {
        try {
            Path path = CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json");
            DynamicOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            if (!Files.exists(path)) {
                initDefaultVals(manager);
                Files.createFile(path);
            } else {
                BufferedReader reader = Files.newBufferedReader(path);
                JsonObject config = GSON.fromJson(reader, JsonObject.class);
                if (config == null)
                    config = new JsonObject();
                reader.close();
                int version = GsonHelper.getAsInt(config, "version", 1);
                if (version < CONFIG_VERSION) {
                    // Legacy config. create a backup and reset to default
                    ImprovedMobs.LOGGER.debug("You are having a legacy config. A Backup will be created");
                    createBackup(manager);
                } else {
                    // Read and update from config
                    EQUIPMENTS = CODEC.parse(ops, config).getPartialOrThrow();
                }
            }
            JsonObject updated = new JsonObject();
            updated.addProperty("version", CONFIG_VERSION);
            updated.add("__comment", commentObj());
            JsonElement data = CODEC.encodeStart(ops, EQUIPMENTS).getOrThrow();
            if (data.isJsonObject()) {
                data.getAsJsonObject().entrySet()
                        .forEach(e -> updated.add(e.getKey(), e.getValue()));
            }
            JsonWriter wr = GSON.newJsonWriter(Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING));
            GSON.toJson(updated, JsonObject.class, wr);
            wr.close();
        } catch (IOException | IllegalStateException e) {
            ImprovedMobs.LOGGER.error("Error initializing equipment file", e);
        }
    }

    private static JsonArray commentObj() {
        JsonArray comment = new JsonArray();
        comment.add("Mobs will be able to equip items declared here");
        comment.add("Value is the item. It also accepts item components. The default config has an example with a harming potion");
        comment.add("Weight is the weight of an item. Higher weight means that the item is more likely to get choosen");
        comment.add("Quality is a modifier applied to the weight. The final weight used is weight + quality * current difficulty");
        return comment;
    }

    private static void createBackup(ItemUseLookupManager manager) {
        try {
            Files.move(CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json"), CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json.bak"));
            initDefaultVals(manager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initDefaultVals(ItemUseLookupManager manager) {
        EQUIPMENTS = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, List<Pair<Integer, OptionalItemStack>>> inverseWeight = new HashMap<>();
        BuiltInRegistries.ITEM.holders().forEach(holder -> {
            EquipmentSlot slot = null;
            Item item = holder.value();
            if (item instanceof BowItem)
                slot = EquipmentSlot.MAINHAND;
            ItemUseHandler ai = manager.get(item).stream().findFirst().orElse(null);
            if (ai != null) {
                slot = ai.defaultedSlot();
            }
            if (item instanceof Equipable equipable && equipable.getEquipmentSlot().getType() != EquipmentSlot.Type.ANIMAL_ARMOR) {
                slot = equipable.getEquipmentSlot();
            }
            if (item instanceof SwordItem || item instanceof DiggerItem)
                if (!defaultBlackLists(item))
                    slot = EquipmentSlot.MAINHAND;
            if (slot != null) {
                OptionalItemStack stack;
                if (item instanceof ThrowablePotionItem) {
                    stack = new OptionalItemStack(holder, m -> m.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING)));
                } else {
                    stack = new OptionalItemStack(holder);
                }
                inverseWeight.computeIfAbsent(slot, k -> new ArrayList<>())
                        .add(Pair.of(score(item), stack));
            }
        });
        for (Map.Entry<EquipmentSlot, List<Pair<Integer, OptionalItemStack>>> entry : inverseWeight.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;
            List<Pair<Integer, OptionalItemStack>> sorted = entry.getValue().stream().sorted(Comparator.comparingInt(Pair::getFirst)).toList();
            int total = entry.getValue().stream().mapToInt(Pair::getFirst).sum();
            float max = 1f / sorted.getFirst().getFirst() * total;
            float median = 1f / sorted.get((int) (sorted.size() * 0.5)).getFirst() * total;
            WeightedItemstackList list = new WeightedItemstackList(entry.getValue().stream()
                    .map(p -> {
                        float[] normalized = normalizeAndInvertWeight(p.getFirst(), max, median, total);
                        return new WeightedItemstack(p.getSecond(), (int) normalized[0], normalized[1]);
                    }).toList());
            EQUIPMENTS.put(entry.getKey(), list);
        }
    }

    private static float[] normalizeAndInvertWeight(int score, float max, float median, int total) {
        float weight = (1f / score) * total;
        float quality = (max - weight) / (median * 1.5f) + 0.5f;
        return new float[]{Math.round(weight * 100), quality * quality};
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("mobbattle");
    }

    private static int score(Item item) {
        int remote = OtherScores.score(item);
        if (remote != -1)
            return remote;
        double score = 0;
        int durability = item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        ItemStack stack = new ItemStack(item);
        double damage = ItemUtils.attribute(stack, Attributes.ATTACK_DAMAGE, 1, EquipmentSlotGroup.values());
        double armor = ItemUtils.attribute(stack, Attributes.ARMOR, 1, EquipmentSlotGroup.values());
        double toughness = ItemUtils.attribute(stack, Attributes.ARMOR_TOUGHNESS, 1, EquipmentSlotGroup.values());
        double knockbackResistance = ItemUtils.attribute(stack, Attributes.KNOCKBACK_RESISTANCE, 1, EquipmentSlotGroup.values());
        int enchantability = item.getEnchantmentValue();
        double multiplier = 1;
        if (item.components().has(DataComponents.UNBREAKABLE)) {
            multiplier *= 2.5;
        }
        if ((armor > 1 || damage > 1) && durability <= 0) {
            durability = 1000;
        }
        if (item instanceof ArmorItem armorItem) {
            try {
                multiplier *= !armorItem.getMaterial().value().repairIngredient().get().isEmpty() ? 1.1 : 1;
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Cannot compute repair ingredient {}", BuiltInRegistries.ITEM.getKey(item), e);
            }
            multiplier *= (armorItem.getMaterial() == ArmorMaterials.LEATHER || armorItem.getMaterial() == ArmorMaterials.GOLD
                    || armorItem.getMaterial() == ArmorMaterials.CHAIN || armorItem.getMaterial() == ArmorMaterials.IRON
                    || armorItem.getMaterial() == ArmorMaterials.DIAMOND || armorItem.getMaterial() == ArmorMaterials.NETHERITE
                    || armorItem.getMaterial() == ArmorMaterials.TURTLE) ? 1.2 : 1;
        } else {
            if (item == Items.FLINT_AND_STEEL)
                score = 800;
            else if (item instanceof ShieldItem)
                score = 1200;
            else if (item instanceof BowItem bow)
                score = 1000 + bow.getDefaultProjectileRange() * 15;
            else if (item instanceof TridentItem)
                multiplier *= 1.1;
            else if (item instanceof CrossbowItem crossbow)
                score = 1000 + crossbow.getDefaultProjectileRange() * 15;
            else if (item instanceof FishingRodItem)
                score = 900;
            else if (item == Items.LAVA_BUCKET)
                score = 2200;
            else if (item == Items.ENDER_PEARL)
                score = 1800;
            else if (item == Items.SNOWBALL)
                score = 500;
            else if (item instanceof ThrowablePotionItem)
                score = 1500;
            else if (item == Items.ENCHANTED_BOOK)
                score = 1900;
            else if (item == Blocks.TNT.asItem())
                score = 2200;
            else if (item == Items.WIND_CHARGE)
                score = 2000;
        }
        score += Math.sqrt(durability) * 15;
        score += Math.max(0, step(damage, new Step(10, v -> (v * v * v * 3 - v * 5) * 2.5),
                new Step(40, v -> v * v * 8 + v * 5 + Math.sqrt(v * 50)),
                new Step(Double.MAX_VALUE, v -> v * v * 6 + v - 5000)));
        score += armor * armor * armor * 25 + armor * armor * 11;
        score += toughness * toughness * 30;
        score += enchantability > 0 ? enchantability * 5 + Math.log(enchantability) * 30 : 0;
        multiplier *= 1 + knockbackResistance * 0.25;
        score *= multiplier;
        return (int) score;
    }

    private static double step(double val, Step... steps) {
        double add = 0;
        double offset = 0;
        for (Step step : steps) {
            if (val <= step.limit) {
                add += step.func().get(val - offset);
                break;
            } else {
                double range = step.limit() - offset;
                add += step.func().get(range);
            }
        }
        return add;
    }

    private record Step(double limit, Double2DoubleFunction func) {

    }
}
