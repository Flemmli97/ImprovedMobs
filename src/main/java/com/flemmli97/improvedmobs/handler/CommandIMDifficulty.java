package com.flemmli97.improvedmobs.handler;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.config.EquipmentList;
import com.flemmli97.improvedmobs.handler.config.EquipmentList.InvalidItemNameException;
import com.flemmli97.improvedmobs.handler.packet.PacketDifficulty;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CommandIMDifficulty implements ICommand {

	private final List<String> aliases = Lists.newArrayList();

	public CommandIMDifficulty() {
		this.aliases.add("improvedMobs");
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "improvedMobs";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "improvedMobs reloadJson | difficulty <set,add> [number] ";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1){
			throw new WrongUsageException(TextFormatting.RED + "Usage: /" + this.getUsage(sender));
		}else{
			if(args[0].equals("reloadJson")){
				try{
					EquipmentList.initEquip(ConfigHandler.config.getConfigFile().getParentFile());
					CommandBase.notifyCommandListener(sender, this, "Reloaded all .json files");
				}catch(InvalidItemNameException e){
					CommandBase.notifyCommandListener(sender, this, TextFormatting.RED + e.getMessage());
				}
			}else if(args[0].equals("difficulty")){
				if(args.length < 2){
					CommandBase.notifyCommandListener(sender, this, TextFormatting.RED + "Usage: /" + this.getUsage(sender));
					return;
				}
				String s = args[1];
				try{
					float f = Float.parseFloat(args[2]);
					if(f * 10 % 1 != 0){
						CommandBase.notifyCommandListener(sender, this, TextFormatting.RED + "Too many decimals. Only 1 supported");
						return;
					}
					if(s.equals("set")){
						DifficultyData data = DifficultyData.get(server.getEntityWorld());
						data.setDifficulty(f);
						PacketHandler.sendToAll(new PacketDifficulty(data));
						CommandBase.notifyCommandListener(sender, this, "Set difficulty to " + f);
					}else if(s.equals("add")){
						DifficultyData data = DifficultyData.get(server.getEntityWorld());
						data.addDifficulty(f);
						PacketHandler.sendToAll(new PacketDifficulty(data));
						CommandBase.notifyCommandListener(sender, this, "Added " + f + " to the difficulty");
					}else
						throw new WrongUsageException(TextFormatting.RED + "Usage: /" + this.getUsage(sender));
				}catch(NumberFormatException e){
					throw new WrongUsageException(TextFormatting.RED + "Usage: /" + this.getUsage(sender));
				}
			}
		}

	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(2, this.getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if(args.length == 1){
			return CommandBase.getListOfStringsMatchingLastWord(args, "reloadJson", "difficulty");
		}
		if(args.length == 2 && args[0].equals("difficulty")){
			return CommandBase.getListOfStringsMatchingLastWord(args, "set", "add");
		}
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

}
