package com.flemmli97.improvedmobs.commands;

import com.flemmli97.improvedmobs.config.EquipmentList;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

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
        data.setDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getWorld());
        src.getSource().sendFeedback(new StringTextComponent("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), true);
        return 1;
    }

    private static int addDifficulty(CommandContext<CommandSource> src) {
        DifficultyData data = DifficultyData.get(src.getSource().getWorld());
        data.addDifficulty(FloatArgumentType.getFloat(src, "val"), src.getSource().getWorld());
        src.getSource().sendFeedback(new StringTextComponent("Difficulty set to " + data.getDifficulty()).setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), true);
        return 1;
    }
}
