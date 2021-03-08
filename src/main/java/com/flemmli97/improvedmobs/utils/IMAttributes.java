package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.entity.MobEntity;

//@Mod.EventBusSubscriber(modid = ImprovedMobs.MODID)
public class IMAttributes {

    /*public static Attribute MAGIC_RES;
    public static Attribute PROJ_BOOST;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Attribute> event) {
        event.getRegistry().registerAll(
                MAGIC_RES = new RangedAttribute("attributes.im.magicRes", 0.0D, 0.0D, 1.0D).setRegistryName(new ResourceLocation(ImprovedMobs.MODID, "magic_res")),
                PROJ_BOOST = new RangedAttribute("attributes.im.projBoost", 0.0D, 0.0D, 100.0D).setRegistryName(new ResourceLocation(ImprovedMobs.MODID, "projectile_boost"))
        );
    }*/

    public static void apply(MobEntity entity, Attribute att, float value, float max) {
        entity.getPersistentData().putFloat(ImprovedMobs.MODID + ":" + att.id, (att == Attribute.PROJ_BOOST ? 1 : 0) + Math.min(value, max));
        //entity.getAttributes().getCustomInstance()
    }

    public static float get(MobEntity entity, Attribute att) {
        return entity.getPersistentData().getFloat(ImprovedMobs.MODID + ":" + att.id);
    }

    public enum Attribute {

        MAGIC_RES("magic_res"),
        PROJ_BOOST("proj_boost");

        private final String id;

        Attribute(String id) {
            this.id = id;
        }
    }
}
