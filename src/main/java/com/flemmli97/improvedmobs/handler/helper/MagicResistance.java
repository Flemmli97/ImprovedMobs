package com.flemmli97.improvedmobs.handler.helper;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;

public class MagicResistance {

    public static final IAttribute MAGIC_RES = (new RangedAttribute((IAttribute)null, "im.magicRes", 0.0D, 0.0D, 1.0D)).setDescription("Magic Resistance");

    public static void apply(EntityMob e)
    {
    	e.getAttributeMap().registerAttribute(MAGIC_RES);
    }
    
    public static float handleMagicRes(EntityMob e, DamageSource source, float damage)
    {
    	if(source.isMagicDamage())
    		damage*=(1-e.getEntityAttribute(MAGIC_RES).getAttributeValue());
    	return damage;
    }  
}
