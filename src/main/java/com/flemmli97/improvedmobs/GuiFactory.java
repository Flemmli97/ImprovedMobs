package com.flemmli97.improvedmobs;

import java.util.Set;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.tenshilib.common.config.ConfigUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiConfig(parentScreen, ConfigUtils.list(ConfigHandler.config), ImprovedMobs.MODID, false, false, ImprovedMobs.MODNAME);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
}
