package io.github.flemmli97.improvedmobs.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class ClientEvents {

    private static float clientDifficulty;
    private static final ResourceLocation tex = new ResourceLocation(ImprovedMobs.MODID, "textures/gui/difficulty_bar.png");

    public static void showDifficulty(PoseStack stack) {
        if (Config.CommonConfig.useScalingHealthMod || !Config.ClientConfig.showDifficulty)
            return;
        stack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, tex);
        Font font = Minecraft.getInstance().font;
        MutableComponent txt = new TranslatableComponent("improvedmobs.overlay.difficulty", String.format(Locale.US, "%.1f", clientDifficulty)).withStyle(Config.ClientConfig.color);
        float scale = Config.ClientConfig.scale;
        stack.scale(scale, scale, scale);
        int width = font.width(txt);
        GuiComponent.blit(stack, Config.ClientConfig.guiX, Config.ClientConfig.guiY, 0, 0, 4 + width, 17, 256, 256);
        GuiComponent.blit(stack, Config.ClientConfig.guiX + 4 + width, Config.ClientConfig.guiY, 183, 0, 3, 17, 256, 256);
        font.draw(stack, new TranslatableComponent("improvedmobs.overlay.difficulty", String.format(java.util.Locale.US, "%.1f", clientDifficulty)).withStyle(Config.ClientConfig.color), Config.ClientConfig.guiX + 4, Config.ClientConfig.guiY + 5, 0);
        stack.popPose();
    }

    public static void updateClientDifficulty(float difficulty) {
        clientDifficulty = difficulty;
    }
}
