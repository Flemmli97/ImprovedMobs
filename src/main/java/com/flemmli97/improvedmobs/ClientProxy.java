package com.flemmli97.improvedmobs;

import com.flemmli97.improvedmobs.entity.InitEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
		InitEntities.initRender();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
     
    @Override
	public IThreadListener getListener(MessageContext ctx) {
    	return ctx.side.isClient() ? Minecraft.getMinecraft() : super.getListener(ctx);
	}
    
    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {

     return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
    }
}