package io.github.flemmli97.improvedmobs.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAI;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.api.config.ExtendedItemStackWrapper;
import io.github.flemmli97.tenshilib.common.utils.ItemUtils;
import io.github.flemmli97.tenshilib.platform.registry.RegistryHelper;
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
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentList {

    private static final Map<EquipmentSlot, WeightedItemstackList> equips = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(Mob e, EquipmentSlot slot, float difficulty) {
        WeightedItemstackList eq = equips.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.getTotalWeight(difficulty) <= 0)
            return ItemStack.EMPTY;
        int index = e.level.random.nextInt(eq.getTotalWeight(difficulty));
        for (WeightedItemstack entry : eq.list) {
            if ((index -= entry.getWeight(difficulty)) >= 0) continue;
            return entry.getItem();
        }
        return ItemStack.EMPTY;
    }

    public static void initEquip() throws InvalidItemNameException {
        try {
            File conf = CrossPlatformStuff.instance().configDirPath().resolve("improvedmobs").resolve("equipment.json").toFile();
            JsonObject confObj = new JsonObject();
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
                List<String> errors = new ArrayList<>();
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
                if (!errors.isEmpty())
                    throw new InvalidItemNameException("Invalid item names for following values: " + errors);
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
        } catch (IOException | IllegalStateException e) {
            ImprovedMobs.logger.error("Error initializing equipment");
            e.printStackTrace();
        }
    }

    private static void initDefaultVals() {
        RegistryHelper.instance().items().getIterator().forEach(item -> {
            if (item instanceof BowItem)
                addItemTo(EquipmentSlot.MAINHAND, item);
            ItemAI ai = ItemAITasks.getAI(item);
            if (ai != null) {
                switch (ai.prefHand()) {
                    case BOTH:
                        if (ai.type() == ItemAI.ItemType.NONSTRAFINGITEM) {
                            float[] weights = getDefaultWeight(item);
                            WeightedItemstack val = new WeightedItemstack(item, (int) weights[0], weights[1]);
                            if (!equips.get(EquipmentSlot.MAINHAND).list.contains(val))
                                equips.compute(EquipmentSlot.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(val) : l.add(val));
                        } else {
                            if (item instanceof ThrowablePotionItem) {
                                String potionItem = RegistryHelper.instance().items().getIDFrom(item).toString() + "{Potion:\"minecraft:harming\"}";
                                float[] weights = getDefaultWeight(item);
                                equips.compute(EquipmentSlot.MAINHAND,
                                        (s, l) -> l == null ? new WeightedItemstackList(new WeightedItemstack(potionItem, (int) weights[0], weights[1], new ArrayList<>())) : l.add(new WeightedItemstack(potionItem, (int) weights[0], weights[1], new ArrayList<>())));
                            } else
                                addItemTo(EquipmentSlot.MAINHAND, item);
                        }
                        break;
                    case MAIN:
                        addItemTo(EquipmentSlot.MAINHAND, item);
                        break;
                    case OFF:
                        addItemTo(EquipmentSlot.OFFHAND, item);
                        break;
                }
            }
            if (item instanceof ArmorItem) {
                switch (((ArmorItem) item).getSlot()) {
                    case FEET -> addItemTo(EquipmentSlot.FEET, item);
                    case CHEST -> addItemTo(EquipmentSlot.CHEST, item);
                    case HEAD -> addItemTo(EquipmentSlot.HEAD, item);
                    case LEGS -> addItemTo(EquipmentSlot.LEGS, item);
                }
            }
            if (item instanceof SwordItem || item instanceof DiggerItem)
                if (!defaultBlackLists(item))
                    addItemTo(EquipmentSlot.MAINHAND, item);
        });
    }

    private static void addItemTo(EquipmentSlot slot, Item item) {
        float[] weights = getDefaultWeight(item);
        equips.compute(slot, (s, l) -> l == null ? new WeightedItemstackList(new WeightedItemstack(item, (int) weights[0], weights[1])) : l.add(new WeightedItemstack(item, (int) weights[0], weights[1])));
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return RegistryHelper.instance().items().getIDFrom(item).getNamespace().equals("mobbattle");
    }

    private static Field techGunDmg, techgunAIAttackTime, techgunAIBurstCount, techgunAIburstAttackTime;
    private static final List<String> defaultZeroWeight = Lists.newArrayList("techguns:nucleardeathray", "techguns:grenadelauncher", "techguns:tfg", "techguns:guidedmissilelauncher", "techguns:rocketlauncher");

    private static float[] getDefaultWeight(Item item) {
        if (defaultZeroWeight.contains(RegistryHelper.instance().items().getIDFrom(item).toString()))
            return new float[]{0, 0};
        int weight = 1500;
        float quality = 0;
        if (item instanceof ArmorItem armor) {
            float fullProt = armor.getMaterial().getDefenseForSlot(EquipmentSlot.HEAD) + armor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST) + armor.getMaterial().getDefenseForSlot(EquipmentSlot.LEGS)
                    + armor.getMaterial().getDefenseForSlot(EquipmentSlot.FEET);
            float toughness = armor.getMaterial().getToughness();
            float averageDurability = (armor.getMaterial().getDurabilityForSlot(EquipmentSlot.HEAD) + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.CHEST) + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.LEGS)
                    + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.FEET)) / 4.0F;
            if (averageDurability < 0)
                averageDurability = 0;
            float ench = armor.getEnchantmentValue();
            float rep = (armor.getMaterial().getRepairIngredient() != null && !armor.getMaterial().getRepairIngredient().isEmpty()) ? 0.9F : 1.15F;
            float vanillaMulti = (armor.getMaterial() == ArmorMaterials.LEATHER || armor.getMaterial() == ArmorMaterials.GOLD || armor.getMaterial() == ArmorMaterials.CHAIN || armor.getMaterial() == ArmorMaterials.IRON
                    || armor.getMaterial() == ArmorMaterials.DIAMOND || armor.getMaterial() == ArmorMaterials.NETHERITE || armor.getMaterial() == ArmorMaterials.TURTLE) ? 0.8F : 1.1F;
            weight -= (fullProt * fullProt * 2.5 + toughness * toughness * 12 + averageDurability * 0.9 + ench) * rep * vanillaMulti;
            quality = defaultQualityFromWeight(weight);
        } else if (item instanceof SwordItem sword) {
            float dmg = 5 + sword.getDamage();
            weight -= (dmg * dmg * 2 + item.getMaxDamage() * 0.3);
            quality = defaultQualityFromWeight(weight);
        } else if (item instanceof DiggerItem) {
            ItemStack def = new ItemStack(item);
            double dmg = 5 + ItemUtils.damage(def);
            weight -= (dmg * dmg * 2 + item.getMaxDamage() * 0.3);
            quality = defaultQualityFromWeight(weight);
        } else {
            if (item == Items.FLINT_AND_STEEL)
                weight = 1200;
            else if (item instanceof ShieldItem)
                weight = 1350;
            else if (item == Items.LAVA_BUCKET)
                weight = 900;
            else if (item == Items.ENDER_PEARL)
                weight = 1100;
            else if (item == Items.SNOWBALL)
                weight = 1400;
            else if (item instanceof ThrowablePotionItem)
                weight = 1050;
            else if (item instanceof BowItem bow)
                weight = (int) (1300 - bow.getMaxDamage() * 0.5);
            else if (item == Items.ENCHANTED_BOOK)
                weight = 1100;
            else if (item == Blocks.TNT.asItem())
                weight = 800;
            else if (item == Items.TRIDENT)
                weight = 900;
            else if (item instanceof CrossbowItem)
                weight = 1000;
        }
        return new float[]{Math.max(weight, 1), quality};
    }

    private static boolean correctQuality(String itemString) {
        String itemReg = itemString;
        if (itemString.contains("{")) {
            int idx = itemString.indexOf("{");
            itemReg = itemString.substring(0, idx);
        }
        Item item = RegistryHelper.instance().items().getFromId(new ResourceLocation(itemReg));
        return item instanceof ArmorItem || item instanceof SwordItem || item instanceof DiggerItem;
    }

    private static float defaultQualityFromWeight(int weight) {
        return (1000 - weight) / 125f;
    }

    public static class WeightedItemstack implements Comparable<WeightedItemstack> {

        private final ExtendedItemStackWrapper item;
        public final String configString; //Cause else nbt value order can be different
        private final int weight;
        private final float quality;

        public WeightedItemstack(Item item, int itemWeight, float quality) {
            this.weight = itemWeight;
            this.quality = quality;
            this.item = new ExtendedItemStackWrapper(RegistryHelper.instance().items().getIDFrom(item).toString());
            this.configString = RegistryHelper.instance().items().getIDFrom(item).toString();
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
            Item it = RegistryHelper.instance().items().getFromId(new ResourceLocation(itemReg));
            if (it == null) {
                errors.add(itemReg);
                this.item = null;
            } else
                this.item = new ExtendedItemStackWrapper(itemReg).setNBT(nbt);
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
            return (RegistryHelper.instance().items().getIDFrom(this.item.getItem()) + (this.item.getTag() != null ? this.item.getTag().toString() : "")).hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return RegistryHelper.instance().items().getIDFrom(this.item.getItem()).toString().compareTo(RegistryHelper.instance().items().getIDFrom(o.item.getItem()).toString());
        }

        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", RegistryHelper.instance().items().getIDFrom(this.item.getItem()), this.weight);
        }

        public int getWeight(float modifier) {
            return Math.max(this.weight + Mth.floor(modifier * this.quality), 0);
        }
    }

    public static class WeightedItemstackList {

        private final List<WeightedItemstack> list = new ArrayList<>();
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
            this.totalWeight = this.list.stream().mapToInt(entry -> entry.getWeight(modifier)).sum();
        }

        public void finishList() {
            this.list.removeIf(w -> (w.weight == 0 && w.quality <= 0) || this.modBlacklist(w.item.getItem()));
        }

        private boolean modBlacklist(Item item) {
            if (Config.CommonConfig.equipmentModWhitelist) {
                for (String s : Config.CommonConfig.equipmentModBlacklist)
                    if (RegistryHelper.instance().items().getIDFrom(item).getNamespace().equals(s))
                        return false;
                return true;
            }
            for (String s : Config.CommonConfig.equipmentModBlacklist)
                if (RegistryHelper.instance().items().getIDFrom(item).getNamespace().equals(s))
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

        /**
         *
         */
        private static final long serialVersionUID = -6736627280613384759L;

        public InvalidItemNameException(String message) {
            super(message);
        }
    }
}
