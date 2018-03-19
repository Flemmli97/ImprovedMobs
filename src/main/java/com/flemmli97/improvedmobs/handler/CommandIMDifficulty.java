package com.flemmli97.improvedmobs.handler;

import java.util.ArrayList;
import java.util.List;

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
	public String getName() {
		return "imDifficulty";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "imDifficulty <set,add> [number]";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2)
        {
			((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getUsage(sender)));
        }
        else
        {
        		String s = args[0];
        		try
        		{
        			float f = Float.parseFloat(args[1]);
        			if(f*10%1!=0)
        			{
        				((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString(TextFormatting.RED + "Too many decimals. Only 1 supported"));
        				return;
        			}
        			if(s.equals("set"))
        			{
        				DifficultyData.get(server.getEntityWorld()).setDifficulty(f);
        				if(sender.getCommandSenderEntity() instanceof EntityPlayer)
        					((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString("Set difficulty to " + f));
        			}
        			else if(s.equals("add"))
        			{
        				DifficultyData.get(server.getEntityWorld()).addDifficulty(f);
        				if(sender.getCommandSenderEntity() instanceof EntityPlayer)
        					((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString("Added " + f + " to the difficulty"));
        			}
        			else
        				((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getUsage(sender)));
        		}
        		catch(NumberFormatException e)
        		{
        			((EntityPlayer)sender.getCommandSenderEntity()).sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /" + this.getUsage(sender)));
        		}
        }
		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(2, this.getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
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
