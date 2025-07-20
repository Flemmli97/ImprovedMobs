package io.github.flemmli97.improvedmobs.common.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.EquipmentList;
import io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.common.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.common.network.PacketHandler;
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

// TODO: make command feedback translatable (test translation lib a bit more before)
public class ImprovedMobsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("improvedmobs")
                .executes(ImprovedMobsCommand::getDifficulty)
                .then(Commands.literal("reloadJson").requires(src -> src.hasPermission(2)).executes(ImprovedMobsCommand::reloadJson))
                .then(Commands.literal("difficulty").requires(src -> src.hasPermission(2))
                        .then(Commands.literal("player").then(Commands.argument("players", GameProfileArgument.gameProfile())
                                .then(Commands.literal("set").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(ImprovedMobsCommand::setDifficultyPlayer)))
                                .then(Commands.literal("add").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(ImprovedMobsCommand::addDifficultyPlayer)))))
                        .then(Commands.literal("set").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(ImprovedMobsCommand::setDifficulty)))
                        .then(Commands.literal("add").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(ImprovedMobsCommand::addDifficulty)))
                        .then(Commands.literal("pause")
                                .then(Commands.literal("player").then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(src -> ImprovedMobsCommand.pauseDifficulty(src, GameProfileArgument.getGameProfiles(src, "players"), true))))
                                .executes(src -> ImprovedMobsCommand.pauseDifficulty(src, null, true)))
                        .then(Commands.literal("unpause")
                                .then(Commands.literal("player").then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(src -> ImprovedMobsCommand.pauseDifficulty(src, GameProfileArgument.getGameProfiles(src, "players"), false))))
                                .executes(src -> ImprovedMobsCommand.pauseDifficulty(src, null, false)))
                        .then(Commands.literal("simulate")
                                .then(Commands.argument("steps", IntegerArgumentType.integer(1))
                                        .then(Commands.literal("player").then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(src -> ImprovedMobsCommand.simulateDifficulty(src, GameProfileArgument.getGameProfiles(src, "players"), IntegerArgumentType.getInteger(src, "steps")))))
                                        .executes(src -> ImprovedMobsCommand.simulateDifficulty(src, null, IntegerArgumentType.getInteger(src, "steps")))))
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
            PlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
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
            PlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
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

    private static int pauseDifficulty(CommandContext<CommandSourceStack> src, Collection<GameProfile> profs, boolean pause) throws CommandSyntaxException {
        if (profs != null) {
            MinecraftServer server = src.getSource().getServer();
            for (GameProfile prof : profs) {
                ServerPlayer player = server.getPlayerList().getPlayer(prof.getId());
                PlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
                data.setPaused(pause);
            }
            src.getSource().sendSuccess(() -> Component.literal("Difficulty " + (pause ? "paused" : "unpaused") + " for given players").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
            return profs.size();
        }
        DifficultyData data = DifficultyData.get(src.getSource().getServer());
        data.setPaused(pause);
        src.getSource().sendSuccess(() -> Component.literal("Difficulty " + (pause ? "paused" : "unpaused")).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        return 1;
    }

    private static int simulateDifficulty(CommandContext<CommandSourceStack> src, Collection<GameProfile> profs, int steps) {
        if (profs != null) {
            MinecraftServer server = src.getSource().getServer();
            for (GameProfile prof : profs) {
                ServerPlayer player = server.getPlayerList().getPlayer(prof.getId());
                PlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
                int i = steps;
                while (i > 0) {
                    float current = data.getDifficultyLevel();
                    data.setDifficultyLevel(current + Config.CommonConfig.difficultyIncrease.get(current).start());
                    i--;
                }
                CrossPlatformStuff.INSTANCE.sendClientboundPacket(PacketHandler.createDifficultyPacket(DifficultyData.get(server), player), player);
            }
            src.getSource().sendSuccess(() -> Component.literal(String.format("Simulated %s difficulty steps for given players", steps)).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
            return profs.size();
        }
        DifficultyData data = DifficultyData.get(src.getSource().getServer());
        int i = steps;
        float current = data.getDifficulty();
        while (i > 0) {
            current += Config.CommonConfig.difficultyIncrease.get(current).start();
            i--;
        }
        data.setDifficulty(current, src.getSource().getServer());
        src.getSource().sendSuccess(() -> Component.literal(String.format("Simulated %s difficulty steps globally. Now at %s", steps, data.getDifficulty())).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), true);
        return 1;
    }
}
