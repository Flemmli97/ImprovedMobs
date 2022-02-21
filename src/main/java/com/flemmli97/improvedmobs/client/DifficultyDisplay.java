package com.flemmli97.improvedmobs.client;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.config.Config;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

public class DifficultyDisplay {

    private static float clientDifficulty;
    private static final ResourceLocation tex = new ResourceLocation(ImprovedMobs.MODID, "textures/gui/difficulty_bar.png");

    @SubscribeEvent
    public void showDifficulty(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || Config.CommonConfig.useScalingHealthMod || !Config.ClientConfig.showDifficulty)
            return;
        MatrixStack stack = e.getMatrixStack();
        stack.push();
        Minecraft.getInstance().getTextureManager().bindTexture(tex);
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        IFormattableTextComponent txt = new TranslationTextComponent("improvedmobs.overlay.difficulty", String.format(Locale.US, "%.1f", clientDifficulty)).mergeStyle(Config.ClientConfig.color);
        float scale = Config.ClientConfig.scale;
        stack.scale(scale, scale, scale);
        int width = font.getStringPropertyWidth(txt);
        AbstractGui.blit(stack, Config.ClientConfig.guiX, Config.ClientConfig.guiY, 0, 0, 4 + width, 17, 256, 256);
        AbstractGui.blit(stack, Config.ClientConfig.guiX + 4 + width, Config.ClientConfig.guiY, 183, 0, 3, 17, 256, 256);
        font.drawText(stack, new TranslationTextComponent("improvedmobs.overlay.difficulty", String.format(java.util.Locale.US, "%.1f", clientDifficulty)).mergeStyle(Config.ClientConfig.color), Config.ClientConfig.guiX + 4, Config.ClientConfig.guiY + 5, 0);
        stack.pop();
    }


    public static void updateClientDifficulty(float difficulty) {
        clientDifficulty = difficulty;
    }
}
