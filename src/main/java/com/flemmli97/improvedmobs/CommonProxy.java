package com.flemmli97.improvedmobs;

import java.io.File;

import com.flemmli97.improvedmobs.entity.InitEntities;
import com.flemmli97.improvedmobs.handler.ConfigHandler;
import com.flemmli97.improvedmobs.handler.DifficultyHandler;
import com.flemmli97.improvedmobs.handler.EventHandlerAI;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCap;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapNetwork;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent e) {
		InitEntities.initEntities();
		ConfigHandler.loadConfig(new Configuration(new File(e.getModConfigurationDirectory()+"/improvedmobs/", "main.cfg")));
		PacketHandler.registerPackets();
    }

    public void init(FMLInitializationEvent e) {
    	CapabilityManager.INSTANCE.register(ITileOpened.class, new TileCapNetwork(), TileCap::new);
    	MinecraftForge.EVENT_BUS.register(new EventHandlerAI());
		if(ConfigHandler.enableDifficultyScaling)
			MinecraftForge.EVENT_BUS.register(new DifficultyHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
    	ConfigHandler.initEquipment();
    	ConfigHandler.breakingItem=ForgeRegistries.ITEMS.getValue(new ResourceLocation(ConfigHandler.breakingItemReg));
    }
    
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
    	 return ctx.getServerHandler().playerEntity;
    	}
}
