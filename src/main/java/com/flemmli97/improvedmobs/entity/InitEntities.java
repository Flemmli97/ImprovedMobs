package com.flemmli97.improvedmobs.entity;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InitEntities {

	public static void initEntities() {
		int entityID = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "snowBall"), EntitySnowBallNew.class, "snowBall", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobTnt"), EntityTntNew.class, "mobTnt", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobSplash"), EntityMobSplash.class, "mobSplash", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "guardianBoat"), EntityGuardianBoat.class, "mobBoat", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobBullet"), EntityMobBullet.class, "mobBullet", ++entityID, ImprovedMobs.instance, 64, 3, true);
	}

	@SideOnly(Side.CLIENT)
	public static void initRender() {
		RenderingRegistry.registerEntityRenderingHandler(EntitySnowBallNew.class, manager -> new RenderSnowball<EntitySnowBallNew>(manager, Items.SNOWBALL, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTntNew.class, RenderTNTPrimed::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMobSplash.class, manager -> new RenderPotion(manager, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGuardianBoat.class, RenderGuardian::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMobBullet.class, RenderShulkerBullet::new);
	}
}
