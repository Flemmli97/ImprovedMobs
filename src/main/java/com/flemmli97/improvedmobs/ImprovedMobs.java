package com.flemmli97.improvedmobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flemmli97.improvedmobs.handler.CommandIMDifficulty;
import com.flemmli97.improvedmobs.handler.ConfigHandler;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;



@Mod(modid = ImprovedMobs.MODID, name = ImprovedMobs.MODNAME, version = ImprovedMobs.VERSION)
public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final String MODNAME = "Better Mob-AI";
    public static final String VERSION = "1.2.4[1.11.2]";
    public static final Logger logger = LogManager.getLogger(ImprovedMobs.MODID);
        
    @Instance
    public static ImprovedMobs instance = new ImprovedMobs();
        
     
    @SidedProxy(clientSide="com.flemmli97.improvedmobs.ClientProxy", serverSide="com.flemmli97.improvedmobs.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
    
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	if(ConfigHandler.enableDifficultyScaling)
    		event.registerServerCommand(new CommandIMDifficulty());
    }
}
    