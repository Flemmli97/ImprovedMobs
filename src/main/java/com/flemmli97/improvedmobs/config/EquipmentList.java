package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.utils.ItemAI;
import com.flemmli97.improvedmobs.utils.ItemAITasks;
import com.flemmli97.tenshilib.api.config.ExtendedItemStackWrapper;
import com.flemmli97.tenshilib.common.utils.ItemUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

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

    private static final Map<EquipmentSlotType, WeightedItemstackList> equips = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(MobEntity e, EquipmentSlotType slot, float difficulty) {
        WeightedItemstackList eq = equips.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.getTotalWeight(difficulty) <= 0)
            return ItemStack.EMPTY;
        int index = e.world.rand.nextInt(eq.getTotalWeight(difficulty));
        for (WeightedItemstack entry : eq.list) {
            if ((index -= entry.getWeight(difficulty)) >= 0) continue;
            return entry.getItem();
        }
        return ItemStack.EMPTY;
    }

    public static void initEquip() throws InvalidItemNameException {
        try {
            File conf = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").resolve("equipment.json").toFile();
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
                for (EquipmentSlotType key : EquipmentSlotType.values()) {
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
            for (EquipmentSlotType key : EquipmentSlotType.values()) {
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
        ForgeRegistries.ITEMS.forEach(item -> {
            if (item instanceof BowItem)
                addItemTo(EquipmentSlotType.MAINHAND, item);
            ItemAI ai = ItemAITasks.getAI(item);
            if (ai != null) {
                switch (ai.prefHand()) {
                    case BOTH:
                        if (ai.type() == ItemAI.ItemType.NONSTRAFINGITEM) {
                            float[] weights = getDefaultWeight(item);
                            WeightedItemstack val = new WeightedItemstack(item, (int) weights[0], weights[1]);
                            if (!equips.get(EquipmentSlotType.MAINHAND).list.contains(val))
                                equips.compute(EquipmentSlotType.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(val) : l.add(val));
                        } else {
                            if (item instanceof ThrowablePotionItem) {
                                String potionItem = item.getRegistryName().toString() + "{Potion:\"minecraft:harming\"}";
                                float[] weights = getDefaultWeight(item);
                                equips.compute(EquipmentSlotType.MAINHAND,
                                        (s, l) -> l == null ? new WeightedItemstackList(new WeightedItemstack(potionItem, (int) weights[0], weights[1], new ArrayList<>())) : l.add(new WeightedItemstack(potionItem, (int) weights[0], weights[1], new ArrayList<>())));
                            } else
                                addItemTo(EquipmentSlotType.MAINHAND, item);
                        }
                        break;
                    case MAIN:
                        addItemTo(EquipmentSlotType.MAINHAND, item);
                        break;
                    case OFF:
                        addItemTo(EquipmentSlotType.OFFHAND, item);
                        break;
                }
            }
            if (item instanceof ArmorItem) {
                switch (((ArmorItem) item).getEquipmentSlot()) {
                    case FEET:
                        addItemTo(EquipmentSlotType.FEET, item);
                        break;
                    case CHEST:
                        addItemTo(EquipmentSlotType.CHEST, item);
                        break;
                    case HEAD:
                        addItemTo(EquipmentSlotType.HEAD, item);
                        break;
                    case LEGS:
                        addItemTo(EquipmentSlotType.LEGS, item);
                        break;
                }
            }
            if (item instanceof SwordItem || item instanceof ToolItem)
                if (!defaultBlackLists(item))
                    addItemTo(EquipmentSlotType.MAINHAND, item);
        });
    }

    private static void addItemTo(EquipmentSlotType slot, Item item) {
        float[] weights = getDefaultWeight(item);
        equips.compute(slot, (s, l) -> l == null ? new WeightedItemstackList(new WeightedItemstack(item, (int) weights[0], weights[1])) : l.add(new WeightedItemstack(item, (int) weights[0], weights[1])));
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof ToolItem && !(item instanceof AxeItem))
            return true;
        return item.getRegistryName().getNamespace().equals("mobbattle");
    }

    private static Field techGunDmg, techgunAIAttackTime, techgunAIBurstCount, techgunAIburstAttackTime;
    private static final List<String> defaultZeroWeight = Lists.newArrayList("techguns:nucleardeathray", "techguns:grenadelauncher", "techguns:tfg", "techguns:guidedmissilelauncher", "techguns:rocketlauncher");

    private static float[] getDefaultWeight(Item item) {
        if (defaultZeroWeight.contains(item.getRegistryName().toString()))
            return new float[]{0, 0};
        int weight = 1500;
        float quality = 0;
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) item;
            float fullProt = armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.HEAD) + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.CHEST) + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.LEGS)
                    + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.FEET);
            float toughness = armor.getArmorMaterial().getToughness();
            float averageDurability = (armor.getArmorMaterial().getDurability(EquipmentSlotType.HEAD) + armor.getArmorMaterial().getDurability(EquipmentSlotType.CHEST) + armor.getArmorMaterial().getDurability(EquipmentSlotType.LEGS)
                    + armor.getArmorMaterial().getDurability(EquipmentSlotType.FEET)) / 4.0F;
            if (averageDurability < 0)
                averageDurability = 0;
            float ench = armor.getItemEnchantability();
            float rep = armor.isRepairable(new ItemStack(armor)) ? 0.9F : 1.15F;
            float vanillaMulti = (armor.getArmorMaterial() == ArmorMaterial.LEATHER || armor.getArmorMaterial() == ArmorMaterial.GOLD || armor.getArmorMaterial() == ArmorMaterial.CHAIN || armor.getArmorMaterial() == ArmorMaterial.IRON
                    || armor.getArmorMaterial() == ArmorMaterial.DIAMOND || armor.getArmorMaterial() == ArmorMaterial.NETHERITE || armor.getArmorMaterial() == ArmorMaterial.TURTLE) ? 0.8F : 1.1F;
            weight -= (fullProt * fullProt * 2.5 + toughness * toughness * 12 + averageDurability * 0.9 + ench) * rep * vanillaMulti;
            quality = defaultQualityFromWeight(weight);
        } else if (item instanceof SwordItem) {
            float dmg = 5 + ((SwordItem) item).getAttackDamage();
            weight -= (dmg * dmg * 2 + item.getMaxDamage() * 0.3);
            quality = defaultQualityFromWeight(weight);
        } else if (item instanceof ToolItem) {
            ItemStack def = new ItemStack(item);
            double dmg = 3 + ItemUtils.damage(def);
            weight -= (dmg * dmg * 2 + item.getMaxDamage() * 0.1);
            quality = defaultQualityFromWeight(weight);
        /*}else if(Config.ServerConfig.useTGunsMod && item instanceof GenericGun){
            float range = ((GenericGun) item).getAI_attackRange();
            if(techGunDmg == null)
                techGunDmg = ReflectionUtils.getField(GenericGun.class, "damageMin");
            if(techgunAIAttackTime == null)
                techgunAIAttackTime = ReflectionUtils.getField(GenericGun.class, "AI_attackTime");
            if(techgunAIBurstCount == null)
                techgunAIBurstCount = ReflectionUtils.getField(GenericGun.class, "AI_burstCount");
            if(techgunAIburstAttackTime == null)
                techgunAIburstAttackTime = ReflectionUtils.getField(GenericGun.class, "AI_burstAttackTime");

            float dmg = ReflectionUtils.getFieldValue(techGunDmg, item);
            int attackTime = ReflectionUtils.getFieldValue(techgunAIAttackTime, item);
            int burstCount = ReflectionUtils.getFieldValue(techgunAIBurstCount, item);
            int burstAttackTime = ReflectionUtils.getFieldValue(techgunAIburstAttackTime, item);

            weight -= 2 * (range * 0.75 + dmg * 14 + attackTime * 0.1 + burstCount * 13 + burstAttackTime * 9) + 300;*/
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
            else if (item instanceof BowItem)
                weight = (int) (1300 - item.getMaxDamage() * 0.5);
            else if (item == Items.ENCHANTED_BOOK)
                weight = 1100;
            else if (item == Blocks.TNT.asItem())
                weight = 800;
            else if (item == Items.TRIDENT)
                weight = 900;
            else if (item instanceof CrossbowItem)
                weight = 1000;
            /*else if(Config.ServerConfig.useReforgedMod){
                if(item instanceof ItemBlowGun)
                    weight = 720;
                else if(item instanceof ItemJavelin)
                    weight = 760;
                else if(item instanceof ItemCrossbow)
                    weight = 800;
                else if(item instanceof ItemBlunderbuss)
                    weight = 740;
            }*/
        }
        return new float[]{Math.max(weight, 1), quality};
    }

    private static boolean correctQuality(String itemString) {
        String itemReg = itemString;
        if (itemString.contains("{")) {
            int idx = itemString.indexOf("{");
            itemReg = itemString.substring(0, idx);
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemReg));
        return item instanceof ArmorItem || item instanceof SwordItem || item instanceof ToolItem;
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
            this.item = new ExtendedItemStackWrapper(item.getRegistryName().toString());
            this.configString = item.getRegistryName().toString();
        }

        public WeightedItemstack(String item, int itemWeight, float quality, List<String> errors) {
            this.weight = itemWeight;
            this.quality = quality;
            this.configString = item;
            String itemReg = item;
            CompoundNBT nbt = null;
            if (item.contains("{")) {
                int idx = item.indexOf("{");
                itemReg = item.substring(0, idx);
                try {
                    nbt = JsonToNBT.getTagFromJson(item.substring(idx));
                } catch (CommandSyntaxException e) {
                    ImprovedMobs.logger.error("Error reading nbt from config {}", item.substring(idx));
                    e.printStackTrace();
                }
            }
            Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemReg));
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
            if (other instanceof WeightedItemstack) {
                WeightedItemstack oth = (WeightedItemstack) other;
                if (!this.item.getItem().getRegistryName().equals(oth.item.getItem().getRegistryName()))
                    return false;
                if (this.item.getTag() == null && oth.item.getTag() != null)
                    return false;
                return this.item.getTag() == null || this.item.getTag().equals(oth.item.getTag());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (this.item.getItem().getRegistryName() + (this.item.getTag() != null ? this.item.getTag().toString() : "")).hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return this.item.getItem().getRegistryName().toString().compareTo(o.item.getItem().getRegistryName().toString());
        }

        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", this.item.getItem().getRegistryName().toString(), this.weight);
        }

        public int getWeight(float modifier) {
            return Math.max(this.weight + MathHelper.floor(modifier * this.quality), 0);
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
                    if (item.getRegistryName().getNamespace().equals(s))
                        return false;
                return true;
            }
            for (String s : Config.CommonConfig.equipmentModBlacklist)
                if (item.getRegistryName().getNamespace().equals(s))
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
