package com.flemmli97.improvedmobs.commands;

import com.flemmli97.improvedmobs.capability.PlayerDifficultyData;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.config.EquipmentList;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.network.PacketHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

public class IMCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("improvedmobs")
                .then(Commands.literal("reloadJson").requires(src -> src.hasPermissionLevel(2)).executes(IMCommand::reloadJson))
                .then(Commands.literal("difficulty").requires(src -> src.hasPermissionLevel(2))
                        .then(Commands.literal("set").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::setDifficulty)))
                        .then(Commands.literal("add").then(Commands.argument("val", FloatArgumentType.floatArg()).executes(IMCommand::addDifficulty)))
                ));
    }

    private static int reloadJson(CommandContext<CommandSource> src) {
        src.getSource().sendFeedback(new StringTextComponent("Reloading equipment.json"), true);
        try {
            EquipmentList.initEquip();
        } catch (EquipmentList.InvalidItemNameException e) {
            src.getSource().sendFeedback(new StringTextComponent(e.getMessage()), false);
        }
        return 1;
    }

    private static int setDifficulty(CommandContext<CommandSource> src) {
        DifficultyData data = DifficultyData.get(src.getSource().getWorld());
        data.setDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getServer());
        src.getSource().sendFeedback(new StringTextComponent("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD)), true);
        return 1;
    }

    private static int addDifficulty(CommandContext<CommandSource> src) {
        DifficultyData data = DifficultyData.get(src.getSource().getWorld());
        data.addDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getServer());
        src.getSource().sendFeedback(new StringTextComponent("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD)), true);
        return 1;
    }

    private static int setDifficultyPlayer(CommandContext<CommandSource> src) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(src, "players");
        MinecraftServer server = src.getSource().getServer();
        for (GameProfile prof : profs) {
            ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(prof.getId());
            PlayerDifficultyData data = TileCapProvider.getPlayerDifficultyData(player);
            data.setDifficultyLevel(FloatArgumentType.getFloat(src, "val"));
            PacketHandler.sendDifficultyToClient(DifficultyData.get(player.world), player);
            src.getSource().sendFeedback(new StringTextComponent("Difficulty for " + prof.getName() + " set to " + data.getDifficultyLevel()).setStyle(Style.EMPTY.mergeWithFormatting(TextFormatting.GOLD)), true);
        }
        return profs.size();
    }

    private static int addDifficultyPlayer(CommandContext<CommandSource> src) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(src, "players");
        MinecraftServer server = src.getSource().getServer();
        for (GameProfile prof : profs) {
            ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(prof.getId());
            PlayerDifficultyData data = TileCapProvider.getPlayerDifficultyData(player);
            data.setDifficultyLevel(data.getDifficultyLevel() + FloatArgumentType.getFloat(src, "val"));
            PacketHandler.sendDifficultyToClient(DifficultyData.get(player.world), player);
            src.getSource().sendFeedback(new StringTextComponent("Difficulty for " + prof.getName() + " set to " + data.getDifficultyLevel()).setStyle(Style.EMPTY.mergeWithFormatting(TextFormatting.GOLD)), true);
        }
        return profs.size();
    }
}
