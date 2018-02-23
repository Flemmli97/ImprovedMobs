package com.flemmli97.improvedmobs.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class EntityMobBoat extends Entity
{

	private int timeWithoutPassenger=0;
    public EntityMobBoat(World worldIn)
    {
        super(worldIn);
        this.setSize(1.375F, 0.5625F);
    }

    public EntityMobBoat(World worldIn, double x, double y, double z)
    {
        this(worldIn);
    }
    
    @Override
    public void onUpdate()
    {
    		if(this.getPassengers().size()==0)
    		{
    			timeWithoutPassenger++;
    			if(timeWithoutPassenger>500)
    				this.setDead();
    		}
    		else
    			timeWithoutPassenger=0;
    		super.onUpdate();
    		if (this.canPassengerSteer())
        {
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
        }
    		else
        {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }
    		this.doBlockCollisions();
        List<Entity> list = this.worldObj.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntitySelectors.<Entity>getTeamCollisionPredicate(this));

        if (!list.isEmpty())
        {
            boolean flag = !this.worldObj.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity = (Entity)list.get(j);

                if (!entity.isPassenger(this))
                {
                    if (flag && this.getPassengers().size() < 1 && !entity.isRiding() && entity.width < this.width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer))
                    {
                        entity.startRiding(this);
                    }
                    else
                    {
                        this.applyEntityCollision(entity);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        if (!this.worldObj.isRemote)
        {
            player.addChatMessage(new TextComponentString(TextFormatting.RED + "You can't ride a mobs boat"));
        }

        return true;
    }

	@Override
	protected void entityInit() {
		
	}
	
	 public boolean attackEntityFrom(DamageSource source, float amount)
	    {
	        if (this.isEntityInvulnerable(source))
	        {
	            return false;
	        }
	        else if (!this.worldObj.isRemote && !this.isDead)
	        {
	            if (source instanceof EntityDamageSourceIndirect && source.getEntity() != null && this.isPassenger(source.getEntity()))
	            {
	                return false;
	            }
	            else
	            {
	                this.setBeenAttacked();
	                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;

	                if (flag)
	                {

	                    this.setDead();
	                }

	                return true;
	            }
	        }
	        else
	        {
	            return true;
	        }
	    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		
	}
	
	@Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return entityIn.getEntityBoundingBox();
    }

    /**
     * Returns the collision bounding box for this entity
     */
	@Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return this.getEntityBoundingBox();
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
	@Override
    public boolean canBePushed()
    {
        return true;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
	@Override
    public double getMountedYOffset()
    {
        return -0.1D;
    }
	
	@Override
	public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }
	
	@Override
	public void applyEntityCollision(Entity entityIn)
    {
        if (entityIn instanceof EntityBoat)
        {
            if (entityIn.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY)
            {
                super.applyEntityCollision(entityIn);
            }
        }
        else if (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY)
        {
            super.applyEntityCollision(entityIn);
        }
    }
	
	@Override
	public boolean canPassengerSteer()
    {
        Entity entity = this.getControllingPassenger();
        return entity instanceof EntityMob ? true : false;
    }
}