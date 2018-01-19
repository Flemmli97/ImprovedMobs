package com.flemmli97.improvedmobs.entity;

import com.flemmli97.improvedmobs.ImprovedMobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPotion;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.init.Items;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InitEntities {
	
	public static void initEntities()
	{
		int entityID = 0;
		EntityRegistry.registerModEntity(EntitySnowBallNew.class, "snowBallDamage", ++entityID, ImprovedMobs.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityTntNew.class, "mobTnt", ++entityID, ImprovedMobs.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityMobSplash.class, "mobSplash", ++entityID, ImprovedMobs.instance, 64, 3, true);
		//EntityRegistry.registerModEntity(EntityMobBoat.class, "mobBoat", ++entityID, ImprovedMobs.instance, 64, 10, true);
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
		
		/*RenderingRegistry.registerEntityRenderingHandler(EntityMobBoat.class, new IRenderFactory<EntityMobBoat>() 
		{
			@Override
			public Render<? super EntityMobBoat> createRenderFor(RenderManager manager) 
			{
				return new RenderBase<>(manager);
			}
		});*/
	}

}
