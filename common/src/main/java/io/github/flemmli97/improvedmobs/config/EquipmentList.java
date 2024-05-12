package io.github.flemmli97.improvedmobs.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAI;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.api.config.ExtendedItemStackWrapper;
import io.github.flemmli97.tenshilib.common.utils.ItemUtils;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class EquipmentList {

    private static final Map<EquipmentSlot, WeightedItemstackList> equips = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(Mob e, EquipmentSlot slot, float difficulty) {
        WeightedItemstackList eq = equips.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.getTotalWeight(difficulty) <= 0)
            return ItemStack.EMPTY;
        int index = e.getRandom().nextInt(eq.getTotalWeight(difficulty));
        for (WeightedItemstack entry : eq.list) {
            if ((index -= entry.getWeight(difficulty)) >= 0) continue;
            return entry.getItem();
        }
        return ItemStack.EMPTY;
    }

    public static void initEquip() throws InvalidItemNameException {
        try {
            File conf = CrossPlatformStuff.INSTANCE.configDirPath().resolve("improvedmobs").resolve("equipment.json").toFile();
            JsonObject confObj = new JsonObject();
            List<String> errors = new ArrayList<>();
            if (!conf.exists()) {
                initDefaultVals();
                conf.createNewFile();
            } else {
                FileReader reader = new FileReader(conf);
                confObj = GSON.fromJson(reader, JsonObject.class);
                if (confObj == null)
                    confObj = new JsonObject();
                reader.close();
                //Read and update from config
                for (EquipmentSlot key : EquipmentSlot.values()) {
                    if (confObj.has(key.toString())) {
                        JsonObject obj = (JsonObject) confObj.get(key.toString());
                        if (!obj.entrySet().isEmpty())
                            obj.entrySet().forEach(ent -> {
                                int weight;
                                float quality;
                                if (ent.getValue().isJsonPrimitive()) {
                                    weight = ent.getValue().getAsInt();
                                    quality = correctQuality(ent.getKey()) ? defaultQualityFromWeight(weight) : 0;
                                } else {
                                    JsonArray entry = ent.getValue().getAsJsonArray();
                                    weight = entry.get(0).getAsInt();
                                    quality = entry.get(1).getAsFloat();
                                }
                                equips.compute(key, (s, l) -> l == null ? new WeightedItemstackList(new WeightedItemstack(ent.getKey(), weight, quality, errors)) : l.add(new WeightedItemstack(ent.getKey(), weight, quality, errors)));
                            });
                        else
                            equips.put(key, new WeightedItemstackList());
                    }
                }
            }
            JsonArray comment = new JsonArray();
            comment.add("Mobs will be able to equip items declared here");
            comment.add("The first number is the weight while the second number is the quality");
            comment.add("Weight is the weight of an item. Higher weight means that the item is more likely to get choosen");
            comment.add("Quality is a modifier applied to the weight. The final weight used is weight + quality * current difficulty");
            confObj.add("__comment", comment);
            for (EquipmentSlot key : EquipmentSlot.values()) {
                JsonObject eq = confObj.has(key.toString()) ? (JsonObject) confObj.get(key.toString()) : new JsonObject();
                equips.get(key).list.forEach(w -> {
                    JsonArray entry = new JsonArray();
                    entry.add(w.weight);
                    entry.add(w.quality);
                    eq.add(w.configString, entry);
                });

                //Sort json object
                JsonObject sorted = new JsonObject();
                List<String> member = new ArrayList<>();
                eq.entrySet().forEach(ent -> member.add(ent.getKey()));
                Collections.sort(member);

                member.forEach(s -> sorted.add(s, eq.get(s)));
                confObj.add(key.toString(), sorted);
                equips.get(key).finishList();
            }
            conf.delete();
            conf.createNewFile();
            JsonWriter wr = GSON.newJsonWriter(new FileWriter(conf));
            GSON.toJson(confObj, JsonObject.class, wr);
            wr.close();
            if (!errors.isEmpty())
                throw new InvalidItemNameException("No items with following names exist: " + errors);
        } catch (IOException | IllegalStateException e) {
            ImprovedMobs.logger.error("Error initializing equipment");
            e.printStackTrace();
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
        PlatformUtils.INSTANCE.items().getIterator().forEach(item -> {
            if (item instanceof BowItem)
                addItemTo(mapBuilder, EquipmentSlot.MAINHAND, item);
            ItemAI ai = ItemAITasks.getAI(item);
            if (ai != null) {
                switch (ai.prefHand()) {
                    case BOTH:
                        if (ai.type() == ItemAI.ItemType.NONSTRAFINGITEM) {
                            float[] weights = getDefaultWeight(item);
                            WeightedItemstack val = new WeightedItemstack(item, (int) weights[0], weights[1]);
                            if (!mapBuilder.get(EquipmentSlot.MAINHAND).contains(val))
                                mapBuilder.compute(EquipmentSlot.OFFHAND, (s, l) -> func.apply(l, val));
                        } else {
                            if (item instanceof ThrowablePotionItem) {
                                String potionItem = PlatformUtils.INSTANCE.items().getIDFrom(item).toString() + "{Potion:\"minecraft:harming\"}";
                                float[] weights = getDefaultWeight(item);
                                mapBuilder.compute(EquipmentSlot.MAINHAND,
                                        (s, l) -> func.apply(l, new WeightedItemstack(potionItem, (int) weights[0], weights[1], new ArrayList<>())));
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
            if (item instanceof ArmorItem) {
                switch (((ArmorItem) item).getEquipmentSlot()) {
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
            entry.getValue().forEach(s -> {
                list.add(new WeightedItemstack(s, s.weight == 0 ? s.weight : max - s.weight + min, s.quality));
            });
            equips.put(entry.getKey(), list);
        }
    }

    private static void addItemTo(Map<EquipmentSlot, List<WeightedItemstack>> map, EquipmentSlot slot, Item item) {
        float[] weights = getDefaultWeight(item);
        map.compute(slot, (s, l) -> {
            if (l == null)
                return new ArrayList<>(List.of(new WeightedItemstack(item, (int) weights[0], weights[1])));
            l.add(new WeightedItemstack(item, (int) weights[0], weights[1]));
            return l;
        });
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return PlatformUtils.INSTANCE.items().getIDFrom(item).getNamespace().equals("mobbattle");
    }

    private static final List<String> DEFAULT_ZERO_WEIGHT = Lists.newArrayList();

    private static int armorSlotDurabilityMod(EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> 13;
            case LEGS -> 15;
            case CHEST -> 16;
            case HEAD -> 11;
            default -> 1;
        };
    }

    private static float[] getDefaultWeight(Item item) {
        if (DEFAULT_ZERO_WEIGHT.contains(PlatformUtils.INSTANCE.items().getIDFrom(item).toString()))
            return new float[]{0, 0};
        int inverseWeight = 0;
        float durability = (float) item.getMaxDamage();
        if (item instanceof ArmorItem armor) {
            float protection = armor.getMaterial().getDefenseForType(ArmorItem.Type.HELMET) + armor.getMaterial().getDefenseForType(ArmorItem.Type.CHESTPLATE) + armor.getMaterial().getDefenseForType(ArmorItem.Type.LEGGINGS)
                    + armor.getMaterial().getDefenseForType(ArmorItem.Type.BOOTS);
            inverseWeight += protection * protection * 4;
            float toughness = armor.getMaterial().getToughness();
            inverseWeight += toughness * toughness * toughness * 0.5;
            durability = durability / armorSlotDurabilityMod(armor.getEquipmentSlot());
            if (durability <= 0)
                durability = 100;
            inverseWeight += durability * 7;
            float enchantmentValue = armor.getEnchantmentValue();
            inverseWeight += enchantmentValue * 5;
            inverseWeight *= (armor.getMaterial().getKnockbackResistance() * 0.5 + 1);
            inverseWeight *= (armor.getMaterial().getRepairIngredient() != null && armor.getMaterial().getRepairIngredient() != Ingredient.EMPTY) ? 1 : 0.9f;
            inverseWeight *= (armor.getMaterial() == ArmorMaterials.LEATHER || armor.getMaterial() == ArmorMaterials.GOLD || armor.getMaterial() == ArmorMaterials.CHAIN || armor.getMaterial() == ArmorMaterials.IRON
                    || armor.getMaterial() == ArmorMaterials.DIAMOND || armor.getMaterial() == ArmorMaterials.NETHERITE || armor.getMaterial() == ArmorMaterials.TURTLE) ? 0.8f : 1;
        } else if (item instanceof SwordItem || item instanceof DiggerItem) {
            ItemStack def = new ItemStack(item);
            double dmg = ItemUtils.damage(def);
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

    private static boolean correctQuality(String itemString) {
        String itemReg = itemString;
        if (itemString.contains("{")) {
            int idx = itemString.indexOf("{");
            itemReg = itemString.substring(0, idx);
        }
        Item item = PlatformUtils.INSTANCE.items().getFromId(new ResourceLocation(itemReg));
        return item instanceof ArmorItem || item instanceof SwordItem || item instanceof DiggerItem;
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

        private final ExtendedItemStackWrapper item;
        public final String configString; //Cause else nbt value order can be different
        private final int weight;
        private final float quality;

        public WeightedItemstack(Item item, int itemWeight, float quality) {
            this.weight = itemWeight;
            this.quality = quality;
            this.item = new ExtendedItemStackWrapper(PlatformUtils.INSTANCE.items().getIDFrom(item).toString());
            this.configString = PlatformUtils.INSTANCE.items().getIDFrom(item).toString();
        }

        public WeightedItemstack(String item, int itemWeight, float quality, List<String> errors) {
            this.weight = itemWeight;
            this.quality = quality;
            this.configString = item;
            String itemReg = item;
            CompoundTag nbt = null;
            if (item.contains("{")) {
                int idx = item.indexOf("{");
                itemReg = item.substring(0, idx);
                try {
                    nbt = TagParser.parseTag(item.substring(idx));
                } catch (CommandSyntaxException e) {
                    ImprovedMobs.logger.error("Error reading nbt from config {}", item.substring(idx));
                    e.printStackTrace();
                }
            }
            Item it = PlatformUtils.INSTANCE.items().getFromId(new ResourceLocation(itemReg));
            if (it == null || (it == Items.AIR && !itemReg.equals("minecraft:air"))) {
                errors.add(itemReg);
                this.item = null;
            } else
                this.item = new ExtendedItemStackWrapper(itemReg).setNBT(nbt);
        }

        private WeightedItemstack(WeightedItemstack other, int itemWeight, float quality) {
            this.weight = itemWeight;
            this.quality = quality;
            this.item = other.item;
            this.configString = other.configString;
        }

        public ItemStack getItem() {
            return this.item.getStack();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this)
                return true;
            if (other instanceof WeightedItemstack oth) {
                if (this.item.getItem() != oth.item.getItem())
                    return false;
                if (this.item.getTag() == null && oth.item.getTag() != null)
                    return false;
                return this.item.getTag() == null || this.item.getTag().equals(oth.item.getTag());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (PlatformUtils.INSTANCE.items().getIDFrom(this.item.getItem()) + (this.item.getTag() != null ? this.item.getTag().toString() : "")).hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return PlatformUtils.INSTANCE.items().getIDFrom(this.item.getItem()).toString().compareTo(PlatformUtils.INSTANCE.items().getIDFrom(o.item.getItem()).toString());
        }

        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", PlatformUtils.INSTANCE.items().getIDFrom(this.item.getItem()), this.weight);
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
                    if (PlatformUtils.INSTANCE.items().getIDFrom(item).getNamespace().equals(s))
                        return false;
                return true;
            }
            for (String s : Config.CommonConfig.equipmentModBlacklist)
                if (PlatformUtils.INSTANCE.items().getIDFrom(item).getNamespace().equals(s))
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
            return String.format("TotalWeight: %d ; [%s]", this.totalWeight, this.list.toString());
        }
    }

    public static class InvalidItemNameException extends Exception {

        @Serial
        private static final long serialVersionUID = -6736627280613384759L;

        public InvalidItemNameException(String message) {
            super(message);
        }
    }
}
