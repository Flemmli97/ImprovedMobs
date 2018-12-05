package com.flemmli97.improvedmobs.handler;

import org.lwjgl.opengl.GL11;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.packet.PacketDifficulty;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DifficultyHandler {
	
	@SubscribeEvent
	public void worldJoin(EntityJoinWorldEvent event)
	{
		if(event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote)
		{
			PacketHandler.sendTo(new PacketDifficulty(DifficultyData.get(event.getEntity().world)), (EntityPlayerMP) event.getEntity());
		}
	}
	
	@SubscribeEvent
    public void increaseDifficulty(WorldTickEvent e)
    {
    		if(e.phase==Phase.END && e.world!=null && !e.world.isRemote && e.world.provider.getDimension()==0)
    		{
    			DifficultyData data = DifficultyData.get(e.world);
    			if(ConfigHandler.general.shouldPunishTimeSkip)
    			{
	    			long timeDiff = (int) Math.abs(e.world.getWorldTime() - data.getPrevTime());
	    			if(timeDiff>2400)
	    			{
	    				long i = timeDiff/2400;
	    				if(timeDiff-i*2400<(i+1)*2400-timeDiff)
	    					i *= 2400;
	    				else
	    					i*=2400+2400;
	    				data.increaseDifficultyBy(e.world.getGameRules().getBoolean("doIMDifficulty")?i/24000F:0, e.world.getWorldTime());
    				}
    			}
    			else
    			{
    				if(e.world.getWorldTime() - data.getPrevTime()>2400)
    				{
    					data.increaseDifficultyBy(e.world.getGameRules().getBoolean("doIMDifficulty")?0.1F:0, e.world.getWorldTime());
    				}
    			}
    		}
    }
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
    public void showDifficulty(RenderGameOverlayEvent.Post e)
    {
		if (e.isCancelable() || e.getType() != ElementType.EXPERIENCE)
			return;
		DifficultyData data = DifficultyData.get(Minecraft.getMinecraft().player.world);
		if(data!=null)
		{
			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			//int x = ConfigHandler.gui.guiX+e.getResolution().getScaledWidth()/2;
			//int y = ConfigHandler.gui.guiY+e.getResolution().getScaledHeight();
			font.drawString(ConfigHandler.gui.color+"Difficulty "+String.format(java.util.Locale.US,"%.1f", data.getDifficulty()), ConfigHandler.gui.guiX, ConfigHandler.gui.guiY, 0);
		}
    }
}
