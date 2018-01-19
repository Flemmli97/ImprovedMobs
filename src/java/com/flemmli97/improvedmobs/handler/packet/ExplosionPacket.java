package com.flemmli97.improvedmobs.handler.packet;

import java.util.List;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.entity.MobExplosion;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExplosionPacket  implements IMessage{

	private double posX;
    private double posY;
    private double posZ;
    private float strength;
    private List<BlockPos> affectedBlockPositions;
    private float motionX;
    private float motionY;
    private float motionZ;	
	public ExplosionPacket(){}
	
	public ExplosionPacket(double xIn, double yIn, double zIn, float strengthIn, List<BlockPos> affectedBlockPositionsIn, Vec3d motion)
	{
		this.posX = xIn;
        this.posY = yIn;
        this.posZ = zIn;
        this.strength = strengthIn;
        this.affectedBlockPositions = Lists.newArrayList(affectedBlockPositionsIn);

        if (motion != null)
        {
            this.motionX = (float)motion.x;
            this.motionY = (float)motion.y;
            this.motionZ = (float)motion.z;
        }	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.posX = (double)buf.readFloat();
        this.posY = (double)buf.readFloat();
        this.posZ = (double)buf.readFloat();
        this.strength = buf.readFloat();
        int i = buf.readInt();
        this.affectedBlockPositions = Lists.<BlockPos>newArrayListWithCapacity(i);
        int j = (int)this.posX;
        int k = (int)this.posY;
        int l = (int)this.posZ;

        for (int i1 = 0; i1 < i; ++i1)
        {
            int j1 = buf.readByte() + j;
            int k1 = buf.readByte() + k;
            int l1 = buf.readByte() + l;
            this.affectedBlockPositions.add(new BlockPos(j1, k1, l1));
        }

        this.motionX = buf.readFloat();
        this.motionY = buf.readFloat();
        this.motionZ = buf.readFloat();	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat((float)this.posX);
        buf.writeFloat((float)this.posY);
        buf.writeFloat((float)this.posZ);
        buf.writeFloat(this.strength);
        buf.writeInt(this.affectedBlockPositions.size());
        int i = (int)this.posX;
        int j = (int)this.posY;
        int k = (int)this.posZ;

        for (BlockPos blockpos : this.affectedBlockPositions)
        {
            int l = blockpos.getX() - i;
            int i1 = blockpos.getY() - j;
            int j1 = blockpos.getZ() - k;
            buf.writeByte(l);
            buf.writeByte(i1);
            buf.writeByte(j1);
        }

        buf.writeFloat(this.motionX);
        buf.writeFloat(this.motionY);
        buf.writeFloat(this.motionZ);	    
	}
	
	 @SideOnly(Side.CLIENT)
	    public float getMotionX()
	    {
	        return this.motionX;
	    }

	    @SideOnly(Side.CLIENT)
	    public float getMotionY()
	    {
	        return this.motionY;
	    }

	    @SideOnly(Side.CLIENT)
	    public float getMotionZ()
	    {
	        return this.motionZ;
	    }

	    @SideOnly(Side.CLIENT)
	    public double getX()
	    {
	        return this.posX;
	    }

	    @SideOnly(Side.CLIENT)
	    public double getY()
	    {
	        return this.posY;
	    }

	    @SideOnly(Side.CLIENT)
	    public double getZ()
	    {
	        return this.posZ;
	    }

	    @SideOnly(Side.CLIENT)
	    public float getStrength()
	    {
	        return this.strength;
	    }

	    @SideOnly(Side.CLIENT)
	    public List<BlockPos> getAffectedBlockPositions()
	    {
	        return this.affectedBlockPositions;
	    }
	
	public static class Handler implements IMessageHandler<ExplosionPacket, IMessage> {

        @Override
        public IMessage onMessage(ExplosionPacket msg, MessageContext ctx) {
        		EntityPlayer player = ImprovedMobs.proxy.getPlayerEntity(ctx);
        		MobExplosion explosion = new MobExplosion(player.world, (Entity)null, msg.getX(), msg.getY(), msg.getZ(), msg.getStrength(), msg.getAffectedBlockPositions());
            explosion.doExplosionB(true);
            player.motionX += (double)msg.getMotionX();
            player.motionY += (double)msg.getMotionY();
            player.motionZ += (double)msg.getMotionZ();	
            return null;
        }
    }
}
