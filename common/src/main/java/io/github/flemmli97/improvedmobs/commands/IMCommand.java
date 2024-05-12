package io.github.flemmli97.improvedmobs.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.network.PacketHandler;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class IMCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("improvedmobs")
                .executes(IMCommand::getDifficulty)
                .then(Commands.literal("reloadJson").requires(src -> src.hasPermission(2)).executes(IMCommand::reloadJson))
                .then(Commands.literal("difficulty").requires(src -> src.hasPermission(2))
                        .then(Commands.literal("player").then(Commands.argument("players", GameProfileArgument.gameProfile())
                                .then(Commands.literal("set").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::setDifficultyPlayer)))
                                .then(Commands.literal("add").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::addDifficultyPlayer)))))
                        .then(Commands.literal("set").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::setDifficulty)))
                        .then(Commands.literal("add").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::addDifficulty)))
                ));
    }

    private static int reloadJson(CommandContext<CommandSourceStack> src) {
        src.getSource().sendSuccess(() -> Component.literal("Reloading equipment.json"), true);
        EquipmentList.initEquip(src.getSource().registryAccess());
        return 1;
    }

    private static int setDifficulty(CommandContext<CommandSourceStack> src) {
        DifficultyData data = DifficultyData.get(src.getSource().getServer());
        data.setDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getServer());
        src.getSource().sendSuccess(() -> Component.literal("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        return 1;
    }

    private static int addDifficulty(CommandContext<CommandSourceStack> src) {
        DifficultyData data = DifficultyData.get(src.getSource().getServer());
        data.addDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getServer());
        src.getSource().sendSuccess(() -> Component.literal("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        return 1;
    }

    private static int setDifficultyPlayer(CommandContext<CommandSourceStack> src) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(src, "players");
        MinecraftServer server = src.getSource().getServer();
        for (GameProfile prof : profs) {
            ServerPlayer player = server.getPlayerList().getPlayer(prof.getId());
            IPlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
            data.setDifficultyLevel(FloatArgumentType.getFloat(src, "val"));
            CrossPlatformStuff.INSTANCE.sendClientboundPacket(PacketHandler.createDifficultyPacket(DifficultyData.get(server), player), player);
            src.getSource().sendSuccess(() -> Component.literal("Difficulty for " + prof.getName() + " set to " + data.getDifficultyLevel()).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        }
        return profs.size();
    }

    private static int addDifficultyPlayer(CommandContext<CommandSourceStack> src) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(src, "players");
        MinecraftServer server = src.getSource().getServer();
        for (GameProfile prof : profs) {
            ServerPlayer player = server.getPlayerList().getPlayer(prof.getId());
            IPlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
            data.setDifficultyLevel(data.getDifficultyLevel() + FloatArgumentType.getFloat(src, "val"));
            CrossPlatformStuff.INSTANCE.sendClientboundPacket(PacketHandler.createDifficultyPacket(DifficultyData.get(server), player), player);
            src.getSource().sendSuccess(() -> Component.literal("Difficulty for " + prof.getName() + " set to " + data.getDifficultyLevel()).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        }
        return profs.size();
    }

    private static int getDifficulty(CommandContext<CommandSourceStack> src) throws CommandSyntaxException {
        float diff;
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL)
            diff = DifficultyData.get(src.getSource().getServer())
                    .getDifficulty();
        else {
            ServerPlayer player = src.getSource().getPlayerOrException();
            diff = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).getDifficultyLevel();
        }
        src.getSource().sendSuccess(() -> Component.literal("Difficulty: " + diff).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        return 1;
    }
}
