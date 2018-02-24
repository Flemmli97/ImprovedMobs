package com.flemmli97.improvedmobs.entity;

import com.flemmli97.improvedmobs.ImprovedMobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPotion;
import net.minecraft.client.renderer.entity.RenderShulkerBullet;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InitEntities {
	
	public static void initEntities()
	{
		int entityID = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "snowBall"), EntitySnowBallNew.class, "snowBall", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobTnt"),EntityTntNew.class, "mobTnt", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobSplash"),EntityMobSplash.class, "mobSplash", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "guardianBoat"),EntityGuardianBoat.class, "mobBoat", ++entityID, ImprovedMobs.instance, 64, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImprovedMobs.MODID, "mobBullet"),EntityMobBullet.class, "mobBullet", ++entityID, ImprovedMobs.instance, 64, 3, true);
	}
	
	@SideOnly(Side.CLIENT)
	public static void initRender()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySnowBallNew.class, new IRenderFactory<EntitySnowBallNew>() 
		{
			@Override
			public Render<? super EntitySnowBallNew> createRenderFor(RenderManager manager) 
			{
				return new RenderSnowball<EntitySnowBallNew>(manager, Items.SNOWBALL, Minecraft.getMinecraft().getRenderItem());
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityTntNew.class, new IRenderFactory<EntityTntNew>() 
		{
			@Override
			public Render<? super EntityTntNew> createRenderFor(RenderManager manager) 
			{
				return new RenderTNTPrimed(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityMobSplash.class, new IRenderFactory<EntityMobSplash>() 
		{
			@Override
			public Render<? super EntityMobSplash> createRenderFor(RenderManager manager) 
			{
				return new RenderPotion(manager, Minecraft.getMinecraft().getRenderItem());
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityGuardianBoat.class, new IRenderFactory<EntityGuardianBoat>() 
		{
			@Override
			public Render<? super EntityGuardianBoat> createRenderFor(RenderManager manager) 
			{
				return new RenderGuardian(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityMobBullet.class, new IRenderFactory<EntityMobBullet>() 
		{
			@Override
			public Render<? super EntityMobBullet> createRenderFor(RenderManager manager) 
			{
				return new RenderShulkerBullet(manager);
			}
		});
	}

}
