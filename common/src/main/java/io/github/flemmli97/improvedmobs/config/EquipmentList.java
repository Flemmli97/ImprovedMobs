package io.github.flemmli97.improvedmobs.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.improvedmobs.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAI;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.tenshilib.RegistryHelper;
import io.github.flemmli97.tenshilib.api.config.ExtendedItemStackWrapper;
import io.github.flemmli97.tenshilib.common.utils.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
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
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentList {

    private static final Map<EquipmentSlot, WeightedItemstackList> equips = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(Mob e, EquipmentSlot slot) {
        WeightedItemstackList eq = equips.get(slot);
        if (eq == null || eq.list.isEmpty() || eq.totalWeight == 0)
            return ItemStack.EMPTY;
        //int totalWeight = (int)Math.max(1, eq.totalWeight*(1-Math.max(DifficultyData.getDifficulty(e.world, e)/312f,0.8)));
        int totalWeight = eq.totalWeight;
        return WeightedRandom.getRandomItem(e.level.random, eq.list, totalWeight).map(WeightedItemstack::getItem).orElse(ItemStack.EMPTY);
    }

    public static void initEquip() throws InvalidItemNameException {
        try {
            //Init default values
            RegistryHelper.items().getIterator().forEach(item -> {
                /*if(Config.ServerConfig.useTGunsMod)
                    if(item instanceof GenericGun)
                        equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                if(Config.ServerConfig.useReforgedMod)
                    if(item instanceof ItemBlowGun || item instanceof ItemJavelin)
                        equips.compute(EquipmentSlotType.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                */
                if (item instanceof BowItem)
                    equips.compute(EquipmentSlot.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));

                ItemAI ai = ItemAITasks.getAI(item);
                if (ai != null) {
                    switch (ai.prefHand()) {
                        case BOTH:
                            if (ai.type() == ItemAI.ItemType.NONSTRAFINGITEM) {
                                WeightedItemstack val = new WeightedItemstack(item, getDefaultWeight(item));
                                if (!equips.get(EquipmentSlot.MAINHAND).list.contains(val))
                                    equips.compute(EquipmentSlot.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(val)) : l.add(val));
                            } else {
                                if (item == Items.SPLASH_POTION) {
                                    String potionItem = RegistryHelper.items().getIDFrom(item).toString() + "{Potion:\"minecraft:harming\"}";
                                    equips.compute(EquipmentSlot.MAINHAND,
                                            (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(potionItem, getDefaultWeight(item), new ArrayList<>()))) : l.add(new WeightedItemstack(potionItem, getDefaultWeight(item), new ArrayList<>())));
                                } else
                                    equips.compute(EquipmentSlot.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            }
                            break;
                        case MAIN:
                            equips.compute(EquipmentSlot.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case OFF:
                            equips.compute(EquipmentSlot.OFFHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                    }
                }
                if (item instanceof ArmorItem) {
                    switch (((ArmorItem) item).getSlot()) {
                        case FEET -> equips.compute(EquipmentSlot.FEET, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                        case CHEST -> equips.compute(EquipmentSlot.CHEST, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                        case HEAD -> equips.compute(EquipmentSlot.HEAD, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                        case LEGS -> equips.compute(EquipmentSlot.LEGS, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                    }
                }
                if (item instanceof SwordItem || item instanceof DiggerItem)
                    if (!defaultBlackLists(item))
                        equips.compute(EquipmentSlot.MAINHAND, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))) : l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            });
            //if(Config.ServerConfig.useReforgedMod)
            //    ItemAITasks.initReforgedStuff();

            File conf = CrossPlatformStuff.configDirPath().resolve("improvedmobs").resolve("equipment.json").toFile();
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
                List<String> errors = new ArrayList<>();
                for (EquipmentSlot key : EquipmentSlot.values()) {
                    if (confObj.has(key.toString())) {
                        JsonObject obj = (JsonObject) confObj.get(key.toString());
                        if (!obj.entrySet().isEmpty())
                            obj.entrySet().forEach(ent -> {
                                int weight = ent.getValue().getAsInt();
                                equips.compute(key, (s, l) -> l == null ? new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(ent.getKey(), weight, errors))) : l.add(new WeightedItemstack(ent.getKey(), weight, errors)));
                            });
                        else
                            equips.put(key, new WeightedItemstackList(new ArrayList<>()));
                    }
                }
                if (!errors.isEmpty())
                    throw new InvalidItemNameException("Invalid item names for following values: " + errors);
            }
            for (EquipmentSlot key : EquipmentSlot.values()) {
                JsonObject eq = confObj.has(key.toString()) ? (JsonObject) confObj.get(key.toString()) : new JsonObject();
                equips.get(key).list.forEach(w -> eq.addProperty(w.configString, w.weight));

                //Sort json object
                JsonObject sorted = new JsonObject();
                List<String> member = new ArrayList<>();
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
        if (item instanceof DiggerItem && !(item instanceof AxeItem))
            return true;
        return RegistryHelper.items().getIDFrom(item).getNamespace().equals("mobbattle");
    }

    private static Field techGunDmg, techgunAIAttackTime, techgunAIBurstCount, techgunAIburstAttackTime;
    private static final List<String> defaultZeroWeight = Lists.newArrayList("techguns:nucleardeathray", "techguns:grenadelauncher", "techguns:tfg", "techguns:guidedmissilelauncher", "techguns:rocketlauncher");

    private static int getDefaultWeight(Item item) {
        if (defaultZeroWeight.contains(RegistryHelper.items().getIDFrom(item).toString()))
            return 0;
        int weight = 1000;
        if (item instanceof ArmorItem armor) {
            float fullProt = armor.getMaterial().getDefenseForSlot(EquipmentSlot.HEAD) + armor.getMaterial().getDefenseForSlot(EquipmentSlot.CHEST) + armor.getMaterial().getDefenseForSlot(EquipmentSlot.LEGS)
                    + armor.getMaterial().getDefenseForSlot(EquipmentSlot.FEET);

            float averageDurability = (armor.getMaterial().getDurabilityForSlot(EquipmentSlot.HEAD) + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.CHEST) + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.LEGS)
                    + armor.getMaterial().getDurabilityForSlot(EquipmentSlot.FEET)) / 4.0F;
            if (averageDurability < 0)
                averageDurability = 0;
            float ench = armor.getEnchantmentValue();
            float rep = !armor.getMaterial().getRepairIngredient().isEmpty() ? 0.9F : 1.15F;
            float vanillaMulti = (armor.getMaterial() == ArmorMaterials.LEATHER || armor.getMaterial() == ArmorMaterials.GOLD || armor.getMaterial() == ArmorMaterials.CHAIN || armor.getMaterial() == ArmorMaterials.IRON
                    || armor.getMaterial() == ArmorMaterials.DIAMOND || armor.getMaterial() == ArmorMaterials.NETHERITE || armor.getMaterial() == ArmorMaterials.TURTLE) ? 0.8F : 1.1F;
            weight -= (fullProt * 3.3 + averageDurability * 0.8 + ench) * rep * vanillaMulti;
        } else if (item instanceof SwordItem) {
            float dmg = 10 + ((SwordItem) item).getDamage();
            weight -= dmg * dmg;
        } else if (item instanceof DiggerItem) {
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

    public static class WeightedItemstack implements WeightedEntry, Comparable<WeightedItemstack> {

        private final ExtendedItemStackWrapper item;
        public final String configString; //Cause else nbt value order can be different
        private final int weight;

        public WeightedItemstack(Item item, int itemWeight) {
            this.weight = itemWeight;
            this.item = new ExtendedItemStackWrapper(RegistryHelper.items().getIDFrom(item).toString());
            this.configString = RegistryHelper.items().getIDFrom(item).toString();

        }

        public WeightedItemstack(String item, int itemWeight, List<String> errors) {
            this.weight = itemWeight;
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
            Item it = RegistryHelper.items().getFromId(new ResourceLocation(itemReg));
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
            return (RegistryHelper.items().getIDFrom(this.item.getItem()) + (this.item.getTag() != null ? this.item.getTag().toString() : "")).hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return RegistryHelper.items().getIDFrom(this.item.getItem()).toString().compareTo(RegistryHelper.items().getIDFrom(o.item.getItem()).toString());
        }

        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", RegistryHelper.items().getIDFrom(this.item.getItem()), this.weight);
        }

        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
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
            this.list.removeIf(w -> w.weight == 0 || this.modBlacklist(w.item.getItem()));
            this.calculateTotalWeight();
        }

        private boolean modBlacklist(Item item) {
            if (Config.CommonConfig.equipmentModWhitelist) {
                for (String s : Config.CommonConfig.equipmentModBlacklist)
                    if (RegistryHelper.items().getIDFrom(item).getNamespace().equals(s))
                        return false;
                return true;
            }
            for (String s : Config.CommonConfig.equipmentModBlacklist)
                if (RegistryHelper.items().getIDFrom(item).getNamespace().equals(s))
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
