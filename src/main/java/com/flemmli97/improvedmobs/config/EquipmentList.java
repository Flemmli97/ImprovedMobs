package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.utils.ItemAITasks;
import com.flemmli97.tenshilib.api.config.ExtendedItemStackWrapper;
import com.flemmli97.tenshilib.common.utils.ItemUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EquipmentList {

    private static final Map<EquipmentSlotType, WeightedItemstackList> equips = Maps.newHashMap();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(MobEntity e, EquipmentSlotType slot) {
        WeightedItemstackList eq = equips.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.totalWeight == 0)
            return ItemStack.EMPTY;
        //int totalWeight = (int)Math.max(1, eq.totalWeight*(1-Math.max(DifficultyData.getDifficulty(e.world, e)/312f,0.8)));
        int totalWeight = eq.totalWeight;
        return WeightedRandom.getRandomItem(e.world.rand, eq.list, totalWeight).getItem();
    }

    public static void initEquip() throws InvalidItemNameException {
        try {
            //Init default values
            ForgeRegistries.ITEMS.forEach(item -> {
                /*if(Config.ServerConfig.useTGunsMod)
                    if(item instanceof GenericGun)
                        equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                if(Config.ServerConfig.useReforgedMod)
                    if(item instanceof ItemBlowGun || item instanceof ItemJavelin)
                        equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                */
                if (item instanceof BowItem)
                    equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));

                ItemAITasks.ItemAI ai = ItemAITasks.getAI(item);
                if (ai != null) {
                    switch (ai.prefHand()) {
                        case BOTH:
                            if (ai.type() == ItemAITasks.ItemType.NONSTRAFINGITEM) {
                                WeightedItemstack val = new WeightedItemstack(item, getDefaultWeight(item));
                                if (!equips.get(EquipmentSlotType.MAINHAND).list.contains(val))
                                    equips.compute(EquipmentSlotType.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(val)) : l.add(val));
                            } else {
                                if (item == Items.SPLASH_POTION) {
                                    String potionItem = item.getRegistryName().toString() + "{Potion:\"minecraft:harming\"}";
                                    equips.compute(EquipmentSlotType.MAINHAND,
                                            (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(potionItem, getDefaultWeight(item), Lists.newArrayList()))) : l.add(new WeightedItemstack(potionItem, getDefaultWeight(item), Lists.newArrayList())));
                                } else
                                    equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            }
                            break;
                        case MAIN:
                            equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case OFF:
                            equips.compute(EquipmentSlotType.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                    }
                }
                if (item instanceof ArmorItem) {
                    switch (((ArmorItem) item).getEquipmentSlot()) {
                        case FEET:
                            equips.compute(EquipmentSlotType.FEET, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case CHEST:
                            equips.compute(EquipmentSlotType.CHEST, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case HEAD:
                            equips.compute(EquipmentSlotType.HEAD, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case LEGS:
                            equips.compute(EquipmentSlotType.LEGS, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                    }
                }
                if (item instanceof SwordItem || item instanceof ToolItem)
                    if (!defaultBlackLists(item))
                        equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            });
            //if(Config.ServerConfig.useReforgedMod)
            //    ItemAITasks.initReforgedStuff();

            File conf = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").resolve("equipment.json").toFile();
            JsonObject confObj = new JsonObject();
            if (!conf.exists()) {
                conf.createNewFile();
            } else {
                //Clear and read all from config
                equips.clear();

                FileReader reader = new FileReader(conf);
                confObj = GSON.fromJson(reader, JsonObject.class);
                if (confObj == null)
                    confObj = new JsonObject();
                reader.close();
                //Read and update from config
                List<String> errors = Lists.newArrayList();
                for (EquipmentSlotType key : EquipmentSlotType.values()) {
                    if (confObj.has(key.toString())) {
                        JsonObject obj = (JsonObject) confObj.get(key.toString());
                        if (!obj.entrySet().isEmpty())
                            obj.entrySet().forEach(ent -> {
                                int weight = ent.getValue().getAsInt();
                                equips.compute(key, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(ent.getKey(), weight, errors))) : l.add(new WeightedItemstack(ent.getKey(), weight, errors)));
                            });
                        else
                            equips.put(key, new WeightedItemstackList(Lists.newArrayList()));
                    }
                }
                if (!errors.isEmpty())
                    throw new InvalidItemNameException("Invalid item names for following values: " + errors);
            }
            for (EquipmentSlotType key : EquipmentSlotType.values()) {
                JsonObject eq = confObj.has(key.toString()) ? (JsonObject) confObj.get(key.toString()) : new JsonObject();
                equips.get(key).list.forEach(w -> eq.addProperty(w.configString, w.itemWeight));

                //Sort json object
                JsonObject sorted = new JsonObject();
                List<String> member = Lists.newArrayList();
                eq.entrySet().forEach(ent -> member.add(ent.getKey()));
                Collections.sort(member);

                member.forEach(s -> sorted.addProperty(s, eq.get(s).getAsInt()));
                confObj.add(key.toString(), sorted);
                equips.get(key).finishList();
            }
            conf.delete();
            conf.createNewFile();
            JsonWriter wr = GSON.newJsonWriter(new FileWriter(conf));
            GSON.toJson(confObj, JsonObject.class, wr);
            wr.close();
        } catch (IOException e) {
            ImprovedMobs.logger.error("Error initializing equipment");
        }
    }

    private static boolean defaultBlackLists(Item item) {
        if (item instanceof ToolItem && !(item instanceof AxeItem))
            return true;
        return item.getRegistryName().getNamespace().equals("mobbattle");
    }

    private static Field techGunDmg, techgunAIAttackTime, techgunAIBurstCount, techgunAIburstAttackTime;
    private static final List<String> defaultZeroWeight = Lists.newArrayList("techguns:nucleardeathray", "techguns:grenadelauncher", "techguns:tfg", "techguns:guidedmissilelauncher", "techguns:rocketlauncher");

    private static int getDefaultWeight(Item item) {
        if (defaultZeroWeight.contains(item.getRegistryName().toString()))
            return 0;
        int weight = 1000;
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) item;
            float fullProt = armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.HEAD) + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.CHEST) + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.LEGS)
                    + armor.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.FEET);

            float averageDurability = (armor.getArmorMaterial().getDurability(EquipmentSlotType.HEAD) + armor.getArmorMaterial().getDurability(EquipmentSlotType.CHEST) + armor.getArmorMaterial().getDurability(EquipmentSlotType.LEGS)
                    + armor.getArmorMaterial().getDurability(EquipmentSlotType.FEET)) / 4.0F;
            if (averageDurability < 0)
                averageDurability = 0;
            float ench = armor.getItemEnchantability();
            float rep = armor.isRepairable(new ItemStack(armor)) ? 0.9F : 1.15F;
            float vanillaMulti = (armor.getArmorMaterial() == ArmorMaterial.LEATHER || armor.getArmorMaterial() == ArmorMaterial.GOLD || armor.getArmorMaterial() == ArmorMaterial.CHAIN || armor.getArmorMaterial() == ArmorMaterial.IRON
                    || armor.getArmorMaterial() == ArmorMaterial.DIAMOND || armor.getArmorMaterial() == ArmorMaterial.NETHERITE || armor.getArmorMaterial() == ArmorMaterial.TURTLE) ? 0.8F : 1.1F;
            weight -= (fullProt * 3.3 + averageDurability * 0.8 + ench) * rep * vanillaMulti;
        } else if (item instanceof SwordItem) {
            float dmg = 10 + ((SwordItem) item).getAttackDamage();
            weight -= dmg * dmg;
        } else if (item instanceof ToolItem) {
            ItemStack def = new ItemStack(item);
            double dmg = 12 + ItemUtils.damage(def);
            weight -= dmg * dmg;
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
                weight = 700;
            else if (item instanceof ShieldItem)
                weight = 850;
            else if (item == Items.LAVA_BUCKET)
                weight = 400;
            else if (item == Items.ENDER_PEARL)
                weight = 600;

            else if (item == Items.SNOWBALL)
                weight = 900;
            else if (item instanceof PotionItem)
                weight = 750;
            else if (item instanceof BowItem)
                weight = 850;
            else if (item == Items.ENCHANTED_BOOK)
                weight = 800;
            else if (item == Blocks.TNT.asItem())
                weight = 600;
            else if (item == Items.TRIDENT)
                weight = 500;
            else if (item instanceof CrossbowItem)
                weight = 700;
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
        return Math.max(weight, 1);
    }

    public static class WeightedItemstack extends WeightedRandom.Item implements Comparable<WeightedItemstack> {

        private final ExtendedItemStackWrapper item;
        public final String configString; //Cause else nbt value order can be different

        public WeightedItemstack(Item item, int itemWeight) {
            super(itemWeight);
            this.item = new ExtendedItemStackWrapper(item.getRegistryName().toString());
            this.configString = item.getRegistryName().toString();

        }

        public WeightedItemstack(String item, int itemWeight, List<String> errors) {
            super(itemWeight);
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
            return String.format("Item: %s; Weight: %d", this.item.getItem().getRegistryName().toString(), this.itemWeight);
        }
    }

    public static class WeightedItemstackList {

        private final List<WeightedItemstack> list;
        private int totalWeight;

        public WeightedItemstackList(List<WeightedItemstack> list) {
            this.list = list;
            this.list.removeIf(w -> w.item == null);
            this.calculateTotalWeight();
        }

        private void calculateTotalWeight() {
            this.totalWeight = WeightedRandom.getTotalWeight(this.list);
        }

        public void finishList() {
            this.list.removeIf(w -> w.itemWeight == 0 || this.modBlacklist(w.item.getItem()));
            this.calculateTotalWeight();
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
