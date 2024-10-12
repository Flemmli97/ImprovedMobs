package io.github.flemmli97.improvedmobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAI;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.common.utils.CodecUtils;
import io.github.flemmli97.tenshilib.common.utils.ItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class EquipmentList {

    private static final int CONFIG_VERSION = 2;

    private static final Map<EquipmentSlot, WeightedItemstackList> EQUIPMENTS = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(Mob e, EquipmentSlot slot, float difficulty) {
        WeightedItemstackList eq = EQUIPMENTS.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.getTotalWeight(difficulty) <= 0)
            return ItemStack.EMPTY;
        int index = e.level().random.nextInt(eq.getTotalWeight(difficulty));
        for (WeightedItemstack entry : eq.filteredList) {
            if ((index -= entry.getWeight(difficulty)) >= 0) continue;
            return entry.getItem();
        }
        return ItemStack.EMPTY;
    }

    public static void initEquip(HolderLookup.Provider provider) {
        try {
            File conf = CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json").toFile();
            JsonObject confObj = new JsonObject();
            RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            if (!conf.exists()) {
                initDefaultVals();
                conf.createNewFile();
            } else {
                FileReader reader = new FileReader(conf);
                confObj = GSON.fromJson(reader, JsonObject.class);
                if (confObj == null)
                    confObj = new JsonObject();
                reader.close();
                int version = GsonHelper.getAsInt(confObj, "version", 1);
                if (version < CONFIG_VERSION) {
                    // Legacy config. create a backup and reset to default
                    ImprovedMobs.LOGGER.debug("You are having a legacy config. A Backup will be created");
                    createBackup();
                } else {
                    //Read and update from config
                    for (EquipmentSlot key : EquipmentSlot.values()) {
                        if (confObj.has(key.toString())) {
                            JsonArray entries = confObj.get(key.toString()).getAsJsonArray();
                            if (!entries.isEmpty())
                                entries.forEach(ent -> {
                                    WeightedItemstack value = WeightedItemstack.CODEC.parse(ops, ent).getOrThrow();
                                    EQUIPMENTS.compute(key, (s, l) -> l == null ? new WeightedItemstackList(value) : l.add(value));
                                });
                            else
                                EQUIPMENTS.put(key, new WeightedItemstackList());
                        }
                    }
                }
            }
            JsonArray comment = new JsonArray();
            comment.add("Mobs will be able to equip items declared here");
            comment.add("Value is the item. It also accepts item components. The default config has an example with a harming potion");
            comment.add("Weight is the weight of an item. Higher weight means that the item is more likely to get choosen");
            comment.add("Quality is a modifier applied to the weight. The final weight used is weight + quality * current difficulty");
            confObj.addProperty("version", CONFIG_VERSION);
            confObj.add("__comment", comment);
            for (EquipmentSlot key : EquipmentSlot.values()) {
                WeightedItemstackList stackList = EQUIPMENTS.get(key);
                if (stackList != null) {
                    //Sort json object
                    JsonArray sorted = new JsonArray();
                    List<WeightedItemstack> sort = new ArrayList<>(stackList.list);
                    Collections.sort(sort);
                    sort.forEach(s -> sorted.add(WeightedItemstack.CODEC.encodeStart(ops, s).getOrThrow()));
                    confObj.add(key.toString(), sorted);
                    stackList.finishList();
                }
            }
            conf.delete();
            conf.createNewFile();
            JsonWriter wr = GSON.newJsonWriter(new FileWriter(conf));
            GSON.toJson(confObj, JsonObject.class, wr);
            wr.close();
        } catch (IOException | IllegalStateException e) {
            ImprovedMobs.LOGGER.error("Error initializing equipment");
            e.printStackTrace();
        }
    }

    private static void createBackup() {
        try {
            Files.move(CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json"), CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json.bak"));
            initDefaultVals();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void initDefaultVals() {
        Map<EquipmentSlot, List<WeightedItemstack>> mapBuilder = new HashMap<>();
        BiFunction<List<WeightedItemstack>, WeightedItemstack, List<WeightedItemstack>> func = (o, v) -> {
            if (o == null)
                return new ArrayList<>(List.of(v));
            o.add(v);
            return o;
        };
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof BowItem)
                addItemTo(mapBuilder, EquipmentSlot.MAINHAND, item);
            ItemAI ai = ItemAITasks.getAI(item);
            if (ai != null) {
                switch (ai.prefHand()) {
                    case BOTH:
                        if (ai.type() == ItemAI.ItemType.NONSTRAFINGITEM) {
                            float[] weights = getDefaultWeight(item);
                            WeightedItemstack val = new WeightedItemstack(new ItemStack(item), (int) weights[0], weights[1]);
                            if (!mapBuilder.get(EquipmentSlot.MAINHAND).contains(val))
                                mapBuilder.compute(EquipmentSlot.OFFHAND, (s, l) -> func.apply(l, val));
                        } else {
                            if (item instanceof ThrowablePotionItem) {
                                float[] weights = getDefaultWeight(item);
                                mapBuilder.compute(EquipmentSlot.MAINHAND,
                                        (s, l) -> func.apply(l, new WeightedItemstack(PotionContents.createItemStack(item, Potions.HARMING), (int) weights[0], weights[1])));
                            } else
                                addItemTo(mapBuilder, EquipmentSlot.MAINHAND, item);
                        }
                        break;
                    case MAIN:
                        addItemTo(mapBuilder, EquipmentSlot.MAINHAND, item);
                        break;
                    case OFF:
                        addItemTo(mapBuilder, EquipmentSlot.OFFHAND, item);
                        break;
                }
            }
            if (item instanceof ArmorItem armorItem) {
                switch (armorItem.getEquipmentSlot()) {
                    case FEET -> addItemTo(mapBuilder, EquipmentSlot.FEET, item);
                    case CHEST -> addItemTo(mapBuilder, EquipmentSlot.CHEST, item);
                    case HEAD -> addItemTo(mapBuilder, EquipmentSlot.HEAD, item);
                    case LEGS -> addItemTo(mapBuilder, EquipmentSlot.LEGS, item);
                }
            }
            if (item instanceof SwordItem || item instanceof DiggerItem)
                if (!defaultBlackLists(item))
                    addItemTo(mapBuilder, EquipmentSlot.MAINHAND, item);
        });
        Map<EquipmentSlot, Pair<Integer, Integer>> minMaxWeight = mapBuilder.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Pair.of(e.getValue().stream().mapToInt(w -> w.weight).min().orElse(1), e.getValue().stream().mapToInt(w -> w.weight).max().orElse(1)),
                (e1, e2) -> e1,
                HashMap::new
        ));
        for (Map.Entry<EquipmentSlot, List<WeightedItemstack>> entry : mapBuilder.entrySet()) {
            WeightedItemstackList list = new WeightedItemstackList();
            int min = (int) (minMaxWeight.get(entry.getKey()).getFirst() * 0.25);
            int max = minMaxWeight.get(entry.getKey()).getSecond();
            entry.getValue().forEach(s -> list.add(new WeightedItemstack(s.item, s.weight == 0 ? s.weight : max - s.weight + min, s.quality)));
            EQUIPMENTS.put(entry.getKey(), list);
        }
    }

    private static void addItemTo(Map<EquipmentSlot, List<WeightedItemstack>> map, EquipmentSlot slot, Item item) {
        float[] weights = getDefaultWeight(item);
        map.compute(slot, (s, l) -> {
            if (l == null)
                return new ArrayList<>(List.of(new WeightedItemstack(new ItemStack(item), (int) weights[0], weights[1])));
            l.add(new WeightedItemstack(new ItemStack(item), (int) weights[0], weights[1]));
            return l;
        });
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("mobbattle");
    }

    private static float[] getDefaultWeight(Item item) {
        int inverseWeight = 0;
        float durability = (float) item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (item instanceof ArmorItem armor) {
            float protection = armor.getMaterial().value().getDefense(ArmorItem.Type.HELMET) + armor.getMaterial().value().getDefense(ArmorItem.Type.CHESTPLATE) + armor.getMaterial().value().getDefense(ArmorItem.Type.LEGGINGS)
                    + armor.getMaterial().value().getDefense(ArmorItem.Type.BOOTS);
            inverseWeight += protection * protection * 4;
            float toughness = armor.getMaterial().value().toughness();
            inverseWeight += toughness * toughness * toughness * 0.5;
            durability = durability / armor.getType().getDurability(1);
            if (durability <= 0)
                durability = 100;
            inverseWeight += durability * 7;
            float enchantmentValue = armor.getEnchantmentValue();
            inverseWeight += enchantmentValue * 5;
            inverseWeight *= (armor.getMaterial().value().knockbackResistance() * 0.5 + 1);
            inverseWeight *= (armor.getMaterial().value().repairIngredient() != null && armor.getMaterial().value().repairIngredient().get() != Ingredient.EMPTY) ? 1 : 0.9f;
            inverseWeight *= (armor.getMaterial() == ArmorMaterials.LEATHER || armor.getMaterial() == ArmorMaterials.GOLD || armor.getMaterial() == ArmorMaterials.CHAIN || armor.getMaterial() == ArmorMaterials.IRON
                    || armor.getMaterial() == ArmorMaterials.DIAMOND || armor.getMaterial() == ArmorMaterials.NETHERITE || armor.getMaterial() == ArmorMaterials.TURTLE) ? 0.8f : 1;
        } else if (item instanceof SwordItem || item instanceof DiggerItem) {
            ItemStack def = new ItemStack(item);
            double dmg = ItemUtils.damageRaw(def);
            if (dmg <= 10)
                inverseWeight += dmg * dmg * 2 + dmg * 3;
            else if (dmg <= 30) {
                dmg -= 10;
                inverseWeight += dmg * dmg * 1.5 + dmg * 5 + 230;
            } else {
                dmg -= 30;
                inverseWeight += dmg * 12 + 930;
            }
            inverseWeight += durability * 1.7;
        } else {
            if (item == Items.FLINT_AND_STEEL)
                inverseWeight = 1000;
            else if (item instanceof ShieldItem)
                inverseWeight = 750;
            else if (item == Items.LAVA_BUCKET)
                inverseWeight = 1500;
            else if (item == Items.ENDER_PEARL)
                inverseWeight = 1100;
            else if (item == Items.SNOWBALL)
                inverseWeight = 550;
            else if (item instanceof ThrowablePotionItem)
                inverseWeight = 1500;
            else if (item instanceof BowItem bow)
                inverseWeight = 1500 + bow.getDefaultProjectileRange() * 20;
            else if (item == Items.ENCHANTED_BOOK)
                inverseWeight = 1600;
            else if (item == Blocks.TNT.asItem())
                inverseWeight = 1700;
            else if (item == Items.TRIDENT)
                inverseWeight = 2000;
            else if (item instanceof CrossbowItem crossbow)
                inverseWeight = 1200 + crossbow.getDefaultProjectileRange() * 18;
            inverseWeight += durability * 2;
        }
        float quality = defaultQualityFromWeight(inverseWeight);
        return new float[]{Math.max(inverseWeight, 1), quality};
    }

    private static float defaultQualityFromWeight(int weight) {
        float multiplier;
        if (weight <= 500) {
            multiplier = 1;
        } else if (weight <= 2000)
            multiplier = (float) Math.log(100 * (weight - 500));
        else
            multiplier = (float) Math.log(500 * (weight - 2000)) + 5;
        multiplier *= 0.01;
        weight = Math.max(1, weight);
        return weight * 0.01f * multiplier;
    }

    public static class WeightedItemstack implements Comparable<WeightedItemstack> {

        public static final Codec<WeightedItemstack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                CodecUtils.ITEM_STACK_CODEC.fieldOf("value").forGetter(d -> d.item),
                Codec.INT.fieldOf("weight").forGetter(d -> d.weight),
                Codec.FLOAT.fieldOf("quality").forGetter(d -> d.quality)
        ).apply(inst, WeightedItemstack::new));

        private final ItemStack item;
        private final int weight;
        private final float quality;

        public WeightedItemstack(ItemStack item, int itemWeight, float quality) {
            this.weight = itemWeight;
            this.quality = quality;
            this.item = item;
        }

        public ItemStack getItem() {
            return this.item.copy();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this)
                return true;
            if (other instanceof WeightedItemstack oth) {
                return ItemStack.matches(this.item, oth.item);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.item.hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString().compareTo(BuiltInRegistries.ITEM.getKey(o.item.getItem()).toString());
        }

        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", BuiltInRegistries.ITEM.getKey(this.item.getItem()), this.weight);
        }

        public int getWeight(float modifier) {
            return Math.max(this.weight + Mth.floor(modifier * this.quality), 0);
        }
    }

    public static class WeightedItemstackList {

        private final List<WeightedItemstack> list = new ArrayList<>();
        private List<WeightedItemstack> filteredList = new ArrayList<>();
        private int totalWeight;
        private float lastModifier = -1;

        public WeightedItemstackList(WeightedItemstack... item) {
            this.list.addAll(Arrays.asList(item));
            this.list.removeIf(w -> w.item == null);
        }

        public int getTotalWeight(float modifier) {
            if (this.lastModifier != modifier) {
                this.lastModifier = modifier;
                this.calculateTotalWeight(this.lastModifier);
            }
            return this.totalWeight;
        }

        private void calculateTotalWeight(float modifier) {
            this.filteredList = this.list.stream().filter(entry -> entry.getWeight(modifier) > 0).toList();
            this.totalWeight = this.filteredList.stream().mapToInt(entry -> entry.getWeight(modifier)).sum();
        }

        public void finishList() {
            this.list.removeIf(w -> (w.weight <= 0 && w.quality <= 0) || this.modBlacklist(w.item.getItem()));
        }

        private boolean modBlacklist(Item item) {
            if (Config.CommonConfig.equipmentModWhitelist) {
                for (String s : Config.CommonConfig.equipmentModBlacklist)
                    if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(s))
                        return false;
                return true;
            }
            for (String s : Config.CommonConfig.equipmentModBlacklist)
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(s))
                    return true;
            return false;
        }

        public WeightedItemstackList add(WeightedItemstack item) {
            if (item.item == null)
                return this;
            this.list.remove(item);
            this.list.add(item);
            return this;
        }

        @Override
        public String toString() {
            return String.format("TotalWeight: %d ; [%s]", this.totalWeight, this.list);
        }
    }
}
