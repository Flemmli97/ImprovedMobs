package com.flemmli97.improvedmobs.handler.packet;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.handler.DifficultyData;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDifficulty implements IMessage {

	public NBTTagCompound compound;

	public PacketDifficulty() {
	}

	public PacketDifficulty(DifficultyData data) {
		this.compound = data.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.compound = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.compound);
	}

	public static class Handler implements IMessageHandler<PacketDifficulty, IMessage> {

		@Override
		public IMessage onMessage(PacketDifficulty msg, MessageContext ctx) {
			ImprovedMobs.proxy.getListener(ctx).addScheduledTask(new Runnable() {

				@Override
				public void run() {
					DifficultyData.get(ImprovedMobs.proxy.getPlayerEntity(ctx).world).readFromNBT(msg.compound);
				}
			});
			return null;
		}
	}
}
