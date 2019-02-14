package com.flemmli97.improvedmobs;

import com.flemmli97.improvedmobs.entity.InitEntities;
import com.flemmli97.improvedmobs.handler.DifficultyHandler;
import com.flemmli97.improvedmobs.handler.EventHandlerAI;
import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCap;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapNetwork;
import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent e) {
		ConfigHandler.load(LoadState.PREINIT);
		InitEntities.initEntities();
		PacketHandler.registerPackets();
    }

    public void init(FMLInitializationEvent e) {
    	CapabilityManager.INSTANCE.register(ITileOpened.class, new TileCapNetwork(), TileCap::new);
		MinecraftForge.EVENT_BUS.register(new EventHandlerAI());
		ConfigHandler.useScalingHealthMod=ConfigHandler.useScalingHealthMod?Loader.isModLoaded("scalinghealth"):false;
		ConfigHandler.useTGunsMod=ConfigHandler.useTGunsMod?Loader.isModLoaded("techguns"):false;
		ConfigHandler.useReforgedMod=ConfigHandler.useReforgedMod?Loader.isModLoaded("reforged"):false;
		if(ConfigHandler.enableDifficultyScaling && !ConfigHandler.useScalingHealthMod)
			MinecraftForge.EVENT_BUS.register(new DifficultyHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
    	ConfigHandler.load(LoadState.POSTINIT);
		ConfigHandler.initEquipment();
    }
    
    public IThreadListener getListener(MessageContext ctx) {
    	return (WorldServer) ctx.getServerHandler().player.world;
    }
    
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
      	 return ctx.getServerHandler().player;
      	}
}
