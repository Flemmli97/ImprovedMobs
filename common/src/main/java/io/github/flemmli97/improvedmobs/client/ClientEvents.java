package io.github.flemmli97.improvedmobs.client;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class ClientEvents {

    private static float clientDifficulty;
    private static final ResourceLocation TEX = ImprovedMobs.modRes("textures/gui/difficulty_bar.png");

    public static void showDifficulty(GuiGraphics graphics) {
        if (!Config.ClientConfig.showDifficultyServerSync || !Config.ClientConfig.showDifficulty)
            return;
        graphics.pose().pushPose();
        Font font = Minecraft.getInstance().font;
        MutableComponent txt = Component.translatable("improvedmobs.overlay.difficulty", String.format(Locale.US, "%.1f", clientDifficulty)).withStyle(Config.ClientConfig.color);
        float scale = Config.ClientConfig.scale;
        graphics.pose().scale(scale, scale, scale);
        int width = font.width(txt);
        int x = Config.ClientConfig.guiX;
        int y = Config.ClientConfig.guiY;
        switch (Config.ClientConfig.location) {
            case TOPRIGHT ->
                    x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 7 - width - Config.ClientConfig.guiX;
            case BOTTOMRIGHT -> {
                x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 7 - width - Config.ClientConfig.guiX;
                y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 17 - Config.ClientConfig.guiY;
            }
            case BOTTOMLEFT ->
                    y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 17 - Config.ClientConfig.guiY;
        }
        graphics.blit(TEX, x, y, 0, 0, 4 + width, 17, 256, 256);
        graphics.blit(TEX, x + 4 + width, y, 183, 0, 3, 17, 256, 256);
        graphics.drawString(font, Component.translatable("improvedmobs.overlay.difficulty", String.format(java.util.Locale.US, "%.1f", clientDifficulty)).withStyle(Config.ClientConfig.color), x + 4, y + 5, 0, false);
        graphics.pose().popPose();
    }

    public static void updateClientDifficulty(float difficulty) {
        clientDifficulty = difficulty;
    }
}
