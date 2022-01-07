package io.github.flemmli97.improvedmobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class ClientEvents {

    private static float clientDifficulty;

    public static void showDifficulty(PoseStack stack) {
        if (Config.CommonConfig.useScalingHealthMod || !Config.ClientConfig.showDifficulty)
            return;
        stack.pushPose();
        float scale = Config.ClientConfig.scale;
        stack.scale(scale, scale, scale);
        Font font = Minecraft.getInstance().font;
        font.draw(stack, Config.ClientConfig.color + "Difficulty " + String.format(java.util.Locale.US, "%.1f", clientDifficulty), Config.ClientConfig.guiX, Config.ClientConfig.guiY, 0);
        stack.popPose();
    }

    public static void updateClientDifficulty(float difficulty) {
        clientDifficulty = difficulty;
    }
}
