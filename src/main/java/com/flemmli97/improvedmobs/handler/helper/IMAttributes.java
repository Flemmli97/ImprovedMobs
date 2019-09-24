package com.flemmli97.improvedmobs.handler.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class IMAttributes {

    public static final IAttribute MAGIC_RES = (new RangedAttribute(null, "im.magicRes", 0.0D, 0.0D, 1.0D)).setDescription("Magic Resistance");
    public static final IAttribute PROJ_BOOST = (new RangedAttribute(null, "im.projBoost", 0.0D, 0.0D, 100.0D)).setDescription("Projectile DMG");

    public static void apply(EntityLivingBase e)
    {
    	e.getAttributeMap().registerAttribute(MAGIC_RES);
    	e.getAttributeMap().registerAttribute(PROJ_BOOST);
    }

}
