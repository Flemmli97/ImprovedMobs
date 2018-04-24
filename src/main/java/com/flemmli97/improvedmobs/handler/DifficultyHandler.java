package com.flemmli97.improvedmobs.handler;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DifficultyHandler {

	public static DifficultyData data;

	@SubscribeEvent
    public void initTracker(WorldEvent.Load e)
    {
    		if(e.getWorld()!=null && !e.getWorld().isRemote)
    			DifficultyHandler.data = DifficultyData.get(e.getWorld());
    }
	
	@SubscribeEvent
    public void increaseDifficulty(WorldTickEvent e)
    {
    		if(e.phase==Phase.END && e.world!=null && !e.world.isRemote)
    		{
    			if(ConfigHandler.shouldPunishTimeSkip)
			{
	    			long timeDiff = (int) Math.abs(e.world.getWorldTime() - data.getPrevTime());
	    			if(timeDiff>2400)
	    			{
	    				long i = timeDiff/2400;
	    				if(timeDiff-i*2400<(i+1)*2400-timeDiff)
	    					i *= 2400;
	    				else
	    					i*=2400+2400;
	    				DifficultyHandler.data.increaseDifficultyBy(i/24000F, e.world.getWorldTime());
    				}
    			}
    			else
    			{
    				if(e.world.getWorldTime() - data.getPrevTime()>2400)
    					DifficultyHandler.data.increaseDifficultyBy(0.1F, e.world.getWorldTime());
    			}
    		}
    }
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
    public void showDifficulty(RenderGameOverlayEvent.Post e)
    {
		if (e.isCancelable() || e.getType() != ElementType.EXPERIENCE)
			return;
		if(data!=null)
		{
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			int x = ConfigHandler.guiX==0?2:ConfigHandler.guiX==1?e.getResolution().getScaledWidth()/2:e.getResolution().getScaledWidth()-2;
			int y = ConfigHandler.guiY==0?2:ConfigHandler.guiY==1?e.getResolution().getScaledHeight()/2:e.getResolution().getScaledHeight()-2;
			if(ConfigHandler.guiX==2)
			{
				String t = "Difficulty "+String.format(java.util.Locale.US,"%.1f", data.getDifficulty());
				font.drawString(t, x-font.getStringWidth(t), y, 0x6d0c9e);
			}
			else if(ConfigHandler.guiX==1)
			{
				String t = "Difficulty "+String.format(java.util.Locale.US,"%.1f", data.getDifficulty());
				font.drawString(t, x-font.getStringWidth(t)/2, y, 0x6d0c9e);
			}
			else
				font.drawString("Difficulty "+String.format(java.util.Locale.US,"%.1f", data.getDifficulty()), x, y, 0x6d0c9e);
		}
    }
}
