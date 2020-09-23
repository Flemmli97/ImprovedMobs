package com.flemmli97.improvedmobs.client;

import com.flemmli97.improvedmobs.config.Config;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DifficultyDisplay {

    private static float clientDifficulty;

    @SubscribeEvent
    public static void showDifficulty(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
            return;
        MatrixStack stack = e.getMatrixStack();
        stack.push();
        float scale = Config.clientConf.scale;
        stack.scale(scale, scale, scale);
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        font.draw(stack, Config.clientConf.color + "Difficulty " + String.format(java.util.Locale.US, "%.1f", clientDifficulty), Config.clientConf.guiX, Config.clientConf.guiY, 0);
        stack.pop();
    }

    public static void updateClientDifficulty(float difficulty) {
        clientDifficulty = difficulty;
    }
}
