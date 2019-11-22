package com.flemmli97.improvedmobs.handler.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.silvercatcher.reforged.items.weapons.ItemBlowGun;
import org.silvercatcher.reforged.items.weapons.ItemBlunderbuss;
import org.silvercatcher.reforged.items.weapons.ItemCrossbow;
import org.silvercatcher.reforged.items.weapons.ItemJavelin;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper.ItemAI;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper.ItemType;
import com.flemmli97.tenshilib.common.item.ItemUtil;
import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import techguns.items.guns.GenericGun;

public class EquipmentList {

    private static final Map<EntityEquipmentSlot,WeightedItemstackList> equips = Maps.newHashMap();
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getEquip(EntityLiving e, EntityEquipmentSlot slot)
    {
        WeightedItemstackList eq = equips.get(slot);
        if(eq==null || eq.list.isEmpty() || eq.totalWeight==0)
            return ItemStack.EMPTY;
        int totalWeight = eq.totalWeight;//(int)Math.max(1, eq.totalWeight*(1-DifficultyData.getDifficulty(e.world, e)/250f));
        return WeightedRandom.getRandomItem(e.world.rand, eq.list, totalWeight).getItem();
    }
    
    public static void initEquip(File confFolder) {
        try {
            
            //Init default values
            ForgeRegistries.ITEMS.forEach(item->{
                
                if(ConfigHandler.useTGunsMod)
                    if(item instanceof GenericGun)
                        equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                        l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                            l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                if(ConfigHandler.useReforgedMod)
                    if(item instanceof ItemBlowGun || item instanceof ItemJavelin)
                        equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                        l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                            l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                if(item instanceof ItemBow)
                    equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                    l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                        l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                
                ItemAI ai=AIUseHelper.getAI(item);
                if(ai!=null) {
                    switch(ai.prefHand()) {
                        case BOTH:
                            
                            if(ai.type()==ItemType.NONSTRAFINGITEM) {
                                WeightedItemstack val = new WeightedItemstack(item, getDefaultWeight(item));
                                if(!equips.get(EntityEquipmentSlot.MAINHAND).list.contains(val))
                                    equips.compute(EntityEquipmentSlot.OFFHAND, (s,l)->
                                    l==null?new WeightedItemstackList(Lists.newArrayList(val)):
                                        l.add(val));
                            }
                            else
                                equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                                l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                                    l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case MAIN:
                            equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                            l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                                l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                        case OFF:
                            equips.compute(EntityEquipmentSlot.OFFHAND, (s,l)->
                            l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                                l.add(new WeightedItemstack(item, getDefaultWeight(item))));
                            break;
                    }
                }
            });
            
            for(Item item : ItemUtil.getList(EntityEquipmentSlot.FEET))
                equips.compute(EntityEquipmentSlot.FEET, (s,l)->
                l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                    l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            
            for(Item item : ItemUtil.getList(EntityEquipmentSlot.CHEST))
                equips.compute(EntityEquipmentSlot.CHEST, (s,l)->
                l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                    l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            
            for(Item item : ItemUtil.getList(EntityEquipmentSlot.HEAD))
                equips.compute(EntityEquipmentSlot.HEAD, (s,l)->
                l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                    l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            
            for(Item item : ItemUtil.getList(EntityEquipmentSlot.LEGS))
                equips.compute(EntityEquipmentSlot.LEGS, (s,l)->
                l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                    l.add(new WeightedItemstack(item, getDefaultWeight(item))));
            
            for(Item item : ItemUtil.getList(EntityEquipmentSlot.MAINHAND))
                if(!defaultBlackLists(item))
                    equips.compute(EntityEquipmentSlot.MAINHAND, (s,l)->
                    l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, getDefaultWeight(item)))):
                        l.add(new WeightedItemstack(item, getDefaultWeight(item))));

            if(ConfigHandler.useReforgedMod)
                AIUseHelper.initReforgedStuff();
            
            
            File conf = new File(confFolder, "equipment.json");
            JsonObject confObj = new JsonObject();
            if(!conf.exists())
            {
                conf.createNewFile();
            }
            else
            {
                FileReader reader = new FileReader(conf);
                confObj = GSON.fromJson(reader, JsonObject.class);
                
                reader.close();
                //Read and update from config
                for(EntityEquipmentSlot key : EntityEquipmentSlot.values())
                {
                    if(confObj.has(key.toString()))
                    {
                        JsonObject obj = (JsonObject) confObj.get(key.toString());
                        obj.entrySet().forEach(ent->{
                            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ent.getKey()));
                            int weight = ent.getValue().getAsInt();
                            equips.compute(key, (s,l)->
                            l==null?new WeightedItemstackList(Lists.newArrayList(new WeightedItemstack(item, weight))):
                                l.add(new WeightedItemstack(item, weight)));
                        });
                    }
                }
                
                conf.delete();
                conf.createNewFile();
            }
            JsonWriter wr = GSON.newJsonWriter(new FileWriter(conf));
            for(EntityEquipmentSlot key : EntityEquipmentSlot.values())
            {
                JsonObject eq = confObj.has(key.toString())?(JsonObject) confObj.get(key.toString()):new JsonObject();
                System.out.println(equips.get(key));
                equips.get(key).list.forEach(w->eq.addProperty(w.item.getRegistryName().toString(), w.itemWeight));
                
                //Sort json object
                JsonObject sorted = new JsonObject();
                List<String> member = Lists.newArrayList();
                eq.entrySet().forEach(ent->member.add(ent.getKey()));              
                Collections.sort(member);
                
                member.forEach(s->sorted.addProperty(s, eq.get(s).getAsInt()));
                confObj.add(key.toString(), sorted); 
                equips.get(key).finishList();
            }
            GSON.toJson(confObj, JsonObject.class, wr);
            wr.close();   
        }
        catch(IOException e)
        {
            ImprovedMobs.logger.error("Error initializing equipment");
        }
    }
    
    private static boolean defaultBlackLists(Item item) {
        if(item instanceof ItemTool && !(item instanceof ItemAxe))
            return true;
        return item.getRegistryName().getResourceDomain().equals("mobbattle");
    }
    
    private static Field techGunDmg;
    private static final List<String> defaultZeroWeight = Lists.newArrayList("techguns:nucleardeathray", "techguns:grenadelauncher","techguns:tfg","techguns:guidedmissilelauncher","techguns:rocketlauncher");
    private static int getDefaultWeight(Item item)
    {
        int weight = 1000;
        if(item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) item;
            float fullProt = armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.HEAD)+armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.CHEST)
                    +armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.LEGS)+armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.FEET);
    
            float averageDurability = (armor.getArmorMaterial().getDurability(EntityEquipmentSlot.HEAD)+armor.getArmorMaterial().getDurability(EntityEquipmentSlot.CHEST)
                    +armor.getArmorMaterial().getDurability(EntityEquipmentSlot.LEGS)+armor.getArmorMaterial().getDurability(EntityEquipmentSlot.FEET))/4.0F;
            if(averageDurability<0)
                averageDurability=0;
            float ench = armor.getItemEnchantability();
            float rep = armor.isRepairable() ? 0.9F:1.15F;
            float vanillaMulti = (armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.LEATHER)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.GOLD)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.CHAIN)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.IRON)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.DIAMOND)) ? 0.8F:1.1F;
            weight-=(fullProt*3.3+averageDurability*0.8+ench)*rep*vanillaMulti;
        }
        else if(item instanceof ItemSword) {
            float dmg = 10+((ItemSword)item).getAttackDamage();
            weight-=dmg*dmg;
        }
        else if(item instanceof ItemTool) {
            ItemStack def = new ItemStack(item);
            double dmg = 12+ItemUtil.damage(def);
            weight-=dmg*dmg;
        }
        else if(ConfigHandler.useTGunsMod && item instanceof GenericGun) {
            float range = ((GenericGun)item).getAI_attackRange();
            if(techGunDmg==null)
                techGunDmg=ReflectionUtils.getField(GenericGun.class, "damageMin");
            float dmg = ReflectionUtils.getFieldValue(techGunDmg, ((GenericGun)item));
            weight-=2*(range*0.75+dmg*12)+450;
        }
        else {
            if(item==Items.FLINT_AND_STEEL)
                weight=700;
            else if(item==Items.SHIELD)
                weight=950;
            else if(item==Items.LAVA_BUCKET)
                weight=400;
            else if(item==Items.ENDER_PEARL)
                weight=600;
            
            else if(item==Items.SNOWBALL)
                weight=900;
            else if(item instanceof ItemPotion)
                weight=750;
            else if(item instanceof ItemBow)
                weight=850;
            else if(item==Items.ENCHANTED_BOOK)
                weight=800;
            else if(item==Item.getItemFromBlock(Blocks.TNT))
                weight=600;
            else if(ConfigHandler.useReforgedMod) {
                if(item instanceof ItemBlowGun)
                    weight=720;
                else if(item instanceof ItemJavelin)
                    weight=760;
                else if(item instanceof ItemCrossbow)
                    weight=800;
                else if(item instanceof ItemBlunderbuss)
                    weight=740;
            }
        }
        return defaultZeroWeight.contains(item.getRegistryName().toString())?0:Math.max(weight, 1);
    }
    
    public static class WeightedItemstack extends WeightedRandom.Item implements Comparable<WeightedItemstack>
    {
        private Item item;
        public WeightedItemstack(Item item, int itemWeightIn) {
            super(itemWeightIn);
            this.item= item;
        }
        
        public ItemStack getItem()
        {
            return new ItemStack(this.item);
        }
        
        @Override
        public boolean equals(Object other)
        {
            if(other == this)
                return true;
            if(other instanceof WeightedItemstack)
                return ((WeightedItemstack) other).item.getRegistryName().equals(this.item.getRegistryName());
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.item.getRegistryName().hashCode();
        }

        @Override
        public int compareTo(WeightedItemstack o) {
            return this.item.getRegistryName().toString().compareTo(o.item.getRegistryName().toString());
        }
        
        @Override
        public String toString() {
            return String.format("Item: %s; Weight: %d", this.item.getRegistryName().toString(), this.itemWeight);
        }
    }
    
    public static class WeightedItemstackList 
    {
        private List<WeightedItemstack> list;
        private int totalWeight;
        
        public WeightedItemstackList(List<WeightedItemstack> list)
        {
            this.list=list;
            this.calculateTotalWeight();
        }
        
        private void calculateTotalWeight()
        {
            this.totalWeight = WeightedRandom.getTotalWeight(this.list);
        }
        
        public void finishList() {
            list.removeIf(w->w.itemWeight==0 || this.modBlacklist(w.item));
            this.calculateTotalWeight();
        }
        
        private boolean modBlacklist(Item item) {
            if(ConfigHandler.equipmentModWhitelist) {
                for(String s : ConfigHandler.equipmentModBlacklist)
                    if(item.getRegistryName().getResourceDomain().equals(s))
                        return false;
                return true;
            }
            for(String s : ConfigHandler.equipmentModBlacklist)
                if(item.getRegistryName().getResourceDomain().equals(s))
                    return true;
            return false;
        }
        
        public WeightedItemstackList add(WeightedItemstack item)
        {
            if(item.item==Items.AIR || item.item==null)
                return this;
            if(this.list.contains(item))
                this.list.remove(item);
            this.list.add(item);
            return this;
        }
        
        @Override
        public String toString() {
            return String.format("TotalWeight: %d ; [%s]", this.totalWeight, this.list.toString());
        }
    }
}
