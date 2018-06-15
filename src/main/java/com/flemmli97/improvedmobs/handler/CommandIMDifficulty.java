package com.flemmli97.improvedmobs.handler;

import java.util.ArrayList;
import java.util.List;

import com.flemmli97.improvedmobs.handler.packet.PacketDifficulty;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandIMDifficulty implements ICommand{

	private final List<String> aliases = new ArrayList<String>();
	public CommandIMDifficulty()
	{
		this.aliases.add("imDifficulty");
	}
	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "imDifficulty";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "imDifficulty <set,add> [number]";
	}

	@Override
	public List<String> getCommandAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2)
        {
			((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getCommandUsage(sender)));
        }
        else
        {
        		String s = args[0];
        		try
        		{
        			float f = Float.parseFloat(args[1]);
        			if(f*10%1!=0)
        			{
        				((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString(TextFormatting.RED + "Too many decimals. Only 1 supported"));
        				return;
        			}
        			if(s.equals("set"))
        			{
        				DifficultyData data = DifficultyData.get(server.getEntityWorld());
        				data.setDifficulty(f);
        				PacketHandler.sendToAll(new PacketDifficulty(data));
        				if(sender.getCommandSenderEntity() instanceof EntityPlayer)
        					((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString("Set difficulty to " + f));
        			}
        			else if(s.equals("add"))
        			{
        				DifficultyData data = DifficultyData.get(server.getEntityWorld());
        				data.addDifficulty(f);
        				PacketHandler.sendToAll(new PacketDifficulty(data));
        				if(sender.getCommandSenderEntity() instanceof EntityPlayer)
        					((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString("Added " + f + " to the difficulty"));
        			}
        			else
        				((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getCommandUsage(sender)));
        		}
        		catch(NumberFormatException e)
        		{
        			((EntityPlayer)sender.getCommandSenderEntity()).addChatMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getCommandUsage(sender)));
        		}
        }
		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canCommandSenderUseCommand(2, this.getCommandName());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"set", "add"});
        }
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}
