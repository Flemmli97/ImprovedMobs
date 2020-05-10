package com.flemmli97.improvedmobs.handler.packet;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.entity.MobExplosion;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class ExplosionPacket implements IMessage {

	private double posX;
	private double posY;
	private double posZ;
	private float strength;
	private List<BlockPos> affectedBlockPositions;
	private double motionX;
	private double motionY;
	private double motionZ;

	public ExplosionPacket() {
	}

	public ExplosionPacket(double xIn, double yIn, double zIn, float strengthIn, List<BlockPos> affectedBlockPositionsIn, Vec3d motion) {
		this.posX = xIn;
		this.posY = yIn;
		this.posZ = zIn;
		this.strength = strengthIn;
		this.affectedBlockPositions = Lists.newArrayList(affectedBlockPositionsIn);

		if(motion != null){
			this.motionX = motion.x;
			this.motionY = motion.y;
			this.motionZ = motion.z;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.posX = buf.readDouble();
		this.posY = buf.readDouble();
		this.posZ = buf.readDouble();
		this.strength = buf.readFloat();
		int i = buf.readInt();
		this.affectedBlockPositions = Lists.newArrayListWithCapacity(i);
		int j = (int) this.posX;
		int k = (int) this.posY;
		int l = (int) this.posZ;

		for(int i1 = 0; i1 < i; ++i1){
			int j1 = buf.readByte() + j;
			int k1 = buf.readByte() + k;
			int l1 = buf.readByte() + l;
			this.affectedBlockPositions.add(new BlockPos(j1, k1, l1));
		}

		this.motionX = buf.readDouble();
		this.motionY = buf.readDouble();
		this.motionZ = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(this.posX);
		buf.writeDouble(this.posY);
		buf.writeDouble(this.posZ);
		buf.writeFloat(this.strength);
		buf.writeInt(this.affectedBlockPositions.size());
		int i = (int) this.posX;
		int j = (int) this.posY;
		int k = (int) this.posZ;

		for(BlockPos blockpos : this.affectedBlockPositions){
			int l = blockpos.getX() - i;
			int i1 = blockpos.getY() - j;
			int j1 = blockpos.getZ() - k;
			buf.writeByte(l);
			buf.writeByte(i1);
			buf.writeByte(j1);
		}

		buf.writeDouble(this.motionX);
		buf.writeDouble(this.motionY);
		buf.writeDouble(this.motionZ);
	}

	public static class Handler implements IMessageHandler<ExplosionPacket, IMessage> {

		@Override
		public IMessage onMessage(ExplosionPacket msg, MessageContext ctx) {
			EntityPlayer player = ImprovedMobs.proxy.getPlayerEntity(ctx);
			MobExplosion explosion = new MobExplosion(player.world, null, msg.posX, msg.posY, msg.posZ, msg.strength, msg.affectedBlockPositions);
			explosion.doExplosionB(true);
			player.motionX += msg.motionX;
			player.motionY += msg.motionY;
			player.motionZ += msg.motionZ;
			return null;
		}
	}
}
