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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentList {

    private static final boolean DEBUG = false;
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
                initDefaultVals(manager, provider);
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
                    createBackup(manager, provider);
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
            if (DEBUG) {
                JsonObject obj = new JsonObject();
                for (Map.Entry<EquipmentSlot, WeightedItemstackList> entry : EQUIPMENTS.entrySet()) {
                    obj.add(entry.getKey().toString(), entry.getValue().asProbability(ops));
                }
                path = path.getParent().resolve("probabilities.json");
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                wr = GSON.newJsonWriter(Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING));
                GSON.toJson(obj, JsonObject.class, wr);
                wr.close();
            }
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

    private static void createBackup(ItemUseLookupManager manager, HolderLookup.Provider provider) {
        try {
            Files.move(CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json"), CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json.bak"));
            initDefaultVals(manager, provider);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initDefaultVals(ItemUseLookupManager manager, HolderLookup.Provider provider) {
        EQUIPMENTS = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, List<Pair<ItemScore, OptionalItemStack>>> inverseWeight = new HashMap<>();
        BuiltInRegistries.ITEM.holders().forEach(holder -> {
            EquipmentSlot slot = null;
            Item item = holder.value();
            if (item instanceof BowItem)
                slot = EquipmentSlot.MAINHAND;
            if (item instanceof Equipable equipable && equipable.getEquipmentSlot().getType() != EquipmentSlot.Type.ANIMAL_ARMOR) {
                slot = equipable.getEquipmentSlot();
            }
            if (item instanceof SwordItem || item instanceof DiggerItem)
                if (!defaultBlackLists(item))
                    slot = EquipmentSlot.MAINHAND;
            ItemUseHandler ai = manager.get(item).stream().findFirst().orElse(null);
            if (ai != null) {
                slot = ai.defaultedSlot();
            }
            if (slot != null) {
                OptionalItemStack stack;
                if (item instanceof ThrowablePotionItem) {
                    stack = new OptionalItemStack(holder, m -> m.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING)));
                } else {
                    stack = new OptionalItemStack(holder);
                }
                ItemScore score = score(item);
                inverseWeight.computeIfAbsent(slot, k -> new ArrayList<>())
                        .add(Pair.of(score, stack));
            }
        });
        OtherScores.addItems(inverseWeight, provider);
        for (Map.Entry<EquipmentSlot, List<Pair<ItemScore, OptionalItemStack>>> entry : inverseWeight.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;
            ItemScore.ScoreRange[] comp = ItemScore.composite(entry.getValue().stream().map(Pair::getFirst).toList());
            WeightedItemstackList list = new WeightedItemstackList(entry.getValue().stream()
                    .map(p -> {
                        float[] normalized = normalizeAndInvertWeight(p.getFirst(), comp[0], comp[1]);
                        return new WeightedItemstack(p.getSecond(), (int) normalized[0], normalized[1]);
                    }).toList());
            EQUIPMENTS.put(entry.getKey(), list);
        }
    }

    private static float[] normalizeAndInvertWeight(ItemScore itemScore, ItemScore.ScoreRange min, ItemScore.ScoreRange max) {
        double dmgNorm = normalize(itemScore.damage(), min.damage(), max.damage());
        double armorNorm = normalize(itemScore.armor(), min.armor(), max.armor());
        double score = normalize(Math.sqrt(itemScore.durability()), Math.sqrt(min.durability()), Math.sqrt(max.durability())) * 25
                + dmgNorm * dmgNorm * 70
                + armorNorm * armorNorm * 70
                + normalize(itemScore.armorToughness(), min.armorToughness(), max.armorToughness()) * 15
                + normalize(itemScore.knockbackResistance(), min.knockbackResistance(), max.knockbackResistance()) * 15
                + normalize(itemScore.enchantmentValue(), min.enchantmentValue(), max.enchantmentValue()) * 5
                + normalize(Math.sqrt(itemScore.utilityScore()), Math.sqrt(min.utilityScore()), Math.sqrt(max.utilityScore())) * 50;
        score *= itemScore.multiplier();
        float weight = score != 0 ? (float) (10000 / score) : 0;
        return new float[]{weight, (float) Math.pow(score, 1 / 3.)};
    }

    private static double normalize(double value, double min, double max) {
        return max == min ? 0.0 : Math.clamp((value - min) / (max - min), 0, 1);
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("mobbattle");
    }

    private static ItemScore score(Item item) {
        double durability = item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        ItemStack stack = new ItemStack(item);
        double damage = ItemUtils.attribute(stack, Attributes.ATTACK_DAMAGE, 0, EquipmentSlotGroup.values());
        double armor = ItemUtils.attribute(stack, Attributes.ARMOR, 0, EquipmentSlotGroup.values());
        if ((armor > 1 || damage > 1) && durability <= 0) {
            durability = 5000;
        }
        if (item.components().has(DataComponents.UNBREAKABLE)) {
            durability *= 3;
        }
        double toughness = ItemUtils.attribute(stack, Attributes.ARMOR_TOUGHNESS, 0, EquipmentSlotGroup.values());
        double knockbackResistance = ItemUtils.attribute(stack, Attributes.KNOCKBACK_RESISTANCE, 0, EquipmentSlotGroup.values());
        int enchantmentValue = item.getEnchantmentValue();
        ItemScore remote = OtherScores.score(item, durability, damage, armor, toughness, knockbackResistance, enchantmentValue);
        if (remote != null)
            return remote;
        double multiplier = 1;
        if (item instanceof ArmorItem armorItem) {
            try {
                multiplier *= !armorItem.getMaterial().value().repairIngredient().get().isEmpty() ? 1.1 : 1;
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Cannot compute repair ingredient {}", BuiltInRegistries.ITEM.getKey(item), e);
            }
            multiplier *= (armorItem.getMaterial() == ArmorMaterials.LEATHER || armorItem.getMaterial() == ArmorMaterials.GOLD
                    || armorItem.getMaterial() == ArmorMaterials.CHAIN || armorItem.getMaterial() == ArmorMaterials.IRON
                    || armorItem.getMaterial() == ArmorMaterials.DIAMOND || armorItem.getMaterial() == ArmorMaterials.NETHERITE
                    || armorItem.getMaterial() == ArmorMaterials.TURTLE) ? 0.9 : 1;
        }
        double utilityScore = 100;
        // MainHand
        if (item instanceof TridentItem) {
            utilityScore = 150;
        } else if (item instanceof CrossbowItem crossbow) {
            utilityScore = Math.max(200 + crossbow.getDefaultProjectileRange() * 10 - Math.sqrt(durability), 100);
        } else if (item == Items.ENCHANTED_BOOK) {
            utilityScore = 500;
        } else if (item == Items.LAVA_BUCKET) {
            utilityScore = 400;
        } else if (item instanceof FishingRodItem) {
            utilityScore = Math.max(185 - Math.sqrt(durability), 75);
        } else if (item == Items.FLINT_AND_STEEL) { // Offhand
            utilityScore = 800;
            damage = 3;
        } else if (item instanceof ShieldItem) {
            utilityScore = Math.max(650 - Math.sqrt(durability), 200);
        } else if (item instanceof BowItem bow) {
            utilityScore = Math.max(250 + bow.getDefaultProjectileRange() * 15 - Math.sqrt(durability), 150);
        } else if (item == Items.ENDER_PEARL) {
            utilityScore = 1600;
        } else if (item == Items.SNOWBALL) {
            utilityScore = 750;
        } else if (item instanceof ThrowablePotionItem) {
            utilityScore = 1400;
            damage = 3;
        } else if (item == Blocks.TNT.asItem()) {
            utilityScore = 2000;
            damage = 15;
        } else if (item == Items.WIND_CHARGE) {
            utilityScore = 1900;
        }
        return new ItemScore(durability, damage, armor, toughness, knockbackResistance, enchantmentValue, utilityScore, multiplier);
    }
}
