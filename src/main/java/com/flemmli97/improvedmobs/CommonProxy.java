package com.flemmli97.improvedmobs;

import java.io.File;

import com.flemmli97.improvedmobs.entity.InitEntities;
import com.flemmli97.improvedmobs.handler.ConfigHandler;
import com.flemmli97.improvedmobs.handler.EventHandlerAI;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent e) {
		InitEntities.initEntities();
		ConfigHandler.loadConfig(new Configuration(new File(e.getModConfigurationDirectory()+"/improvedmobs/", "main.cfg")));
		PacketHandler.registerPackets();
    }

    public void init(FMLInitializationEvent e) {
    	MinecraftForge.EVENT_BUS.register(new EventHandlerAI());
    }

    public void postInit(FMLPostInitializationEvent e) {
    	ConfigHandler.write();
    }
    
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
     	 return ctx.getServerHandler().playerEntity;
     	}
}
