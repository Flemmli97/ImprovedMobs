package com.flemmli97.improvedmobs.handler.packet;

import com.flemmli97.improvedmobs.ImprovedMobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PathDebugging implements IMessage {

	public int x, y, z, id;

	public PathDebugging() {
	}

	public PathDebugging(int particleId, int x, int y, int z) {
		this.id = particleId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.x = compound.getIntArray("pos")[0];
        this.y = compound.getIntArray("pos")[1];
        this.z = compound.getIntArray("pos")[2];
        this.id = compound.getIntArray("pos")[3];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setIntArray("pos", new int[] {this.x, this.y, this.z, this.id});
		ByteBufUtils.writeTag(buf, compound);
	}

	public static class Handler implements IMessageHandler<PathDebugging, IMessage> {

		@Override
		public IMessage onMessage(PathDebugging msg, MessageContext ctx) {
			EntityPlayer player = ImprovedMobs.proxy.getPlayerEntity(ctx);
			if(player != null){
				player.world.spawnParticle(EnumParticleTypes.getParticleFromId(msg.id), true, msg.x + 0.5, msg.y + 0.2, msg.z + 0.5, 0, 0, 0);
			}
			return null;
		}
	}
}
