package com.flemmli97.improvedmobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;



@Mod(modid = ImprovedMobs.MODID, name = ImprovedMobs.MODNAME, version = ImprovedMobs.VERSION)
public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final String MODNAME = "Better Mob-AI";
    public static final String VERSION = "1.1.3[1.10.2]";
    public static final Logger logger = LogManager.getLogger(ImprovedMobs.MODID);
        
    @Instance
    public static ImprovedMobs instance = new ImprovedMobs();
        
     
    @SidedProxy(clientSide="com.flemmli97.improvedmobs.ClientProxy", serverSide="com.flemmli97.improvedmobs.ServerProxy")
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
}
    