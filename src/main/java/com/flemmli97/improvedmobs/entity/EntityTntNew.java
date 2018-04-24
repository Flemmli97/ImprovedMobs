package com.flemmli97.improvedmobs.entity;

import com.flemmli97.improvedmobs.handler.packet.ExplosionPacket;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityTntNew extends EntityTNTPrimed{

    private int fuse;
    private static final DataParameter<Integer> FUSENEW = EntityDataManager.<Integer>createKey(EntityTNTPrimed.class, DataSerializers.VARINT);
    private EntityLivingBase tntPlacedBy;

	public EntityTntNew(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit()
    {
        this.dataManager.register(FUSENEW, 80);
    }

	public EntityTntNew(World worldIn, double x, double y, double z, EntityLivingBase igniter) 
	{
		this(worldIn);
        this.setPosition(x, y, z);
        float f = (float)(Math.random() * (Math.PI * 2D));
        this.motionX = (double)(-((float)Math.sin((double)f)) * 0.02F);
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)(-((float)Math.cos((double)f)) * 0.02F);
        this.setFuse(80);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.tntPlacedBy = igniter;	}
	
	public void setHeadingFromThrower(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
    {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.setThrowableHeading((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround)
        {
            this.motionY += entityThrower.motionY;
        }
    	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt_double(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt_double(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }
	 
	@Override
    public void onUpdate()
    {
		this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity())
        {
            this.motionY -= 0.03999999910593033D;
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
        }

        --this.fuse;

        if (this.fuse <= 0)
        {
            this.setDead();

            if (!this.worldObj.isRemote)
            {
                this.explode();
            }
        }
        else
        {
            this.handleWaterMovement();
            this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
        }
    }

    private void explode()
    {
        MobExplosion explosion = new MobExplosion(this.worldObj, this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, 5.0F, false, true);
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        for (EntityPlayer entityplayer : this.worldObj.playerEntities)
        {
            if (entityplayer instanceof EntityPlayerMP && entityplayer.getDistanceSq(this.posX, this.posY + (double)(this.height / 16.0F), this.posZ) < 4096.0D)
            {
            		PacketHandler.sendTo(new ExplosionPacket(this.posX, this.posY, this.posZ, 5.0F, explosion.getAffectedBlockPositions(), (Vec3d)explosion.getPlayerKnockbackMap().get(entityplayer)), (EntityPlayerMP) entityplayer);            
            }
        }
    }
    
    public void setFuse(int fuseIn)
    {
        this.dataManager.set(FUSENEW, fuseIn);
        this.fuse = fuseIn;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (FUSENEW.equals(key))
        {
            this.fuse = this.getFuseDataManager();
        }
    }
    
    @Override
    public EntityLivingBase getTntPlacedBy()
    {
        return this.tntPlacedBy;
    }

    /**
     * Gets the fuse from the data manager
     */
    @Override
    public int getFuseDataManager()
    {
        return ((Integer)this.dataManager.get(FUSENEW)).intValue();
    }

    public int getFuse()
    {
        return this.fuse;
    }
}
