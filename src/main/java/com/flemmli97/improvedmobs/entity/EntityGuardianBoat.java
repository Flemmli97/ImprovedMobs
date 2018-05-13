package com.flemmli97.improvedmobs.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityGuardianBoat extends EntityGuardian{

	private int timeWithoutPassenger;
	private int jumpingTick;
	public EntityGuardianBoat(World worldIn) {
		super(worldIn);
		this.experienceValue=0;
		this.tasks.taskEntries.removeAll(this.tasks.taskEntries);	
		this.targetTasks.taskEntries.removeAll(this.targetTasks.taskEntries);	
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D, 80));
        this.tasks.addTask(9, new EntityAILookIdle(this));
	}
	
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
    }

	public void onLivingUpdate()
    {
		if(this.getPassengers().isEmpty() || ((EntityMob)this.getPassengers().get(0)).getAttackTarget()==null)
		{
			timeWithoutPassenger++;
			if(timeWithoutPassenger>500)
				this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
		}
		else
			timeWithoutPassenger=0;
		if(!this.getPassengers().isEmpty())
		{
			if(this.getPassengers().get(0) instanceof EntityMob)
			{
				EntityLivingBase target = ((EntityMob)this.getPassengers().get(0)).getAttackTarget();
				((EntityMob)this.getPassengers().get(0)).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:water_breathing"),10,1, false, false));
				if(target!=null)
				{
					this.getNavigator().clearPathEntity();
					this.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 1);
				}
			}
		}
		if(this.isInWater())
		{
			if(this.nearShore(0))
			{
				this.jumpingTick=20;
				this.motionY=1;
				Vec3d facing = this.getLookVec().scale(0.5);
				this.motionX+=facing.xCoord;
				this.motionZ+=facing.zCoord;
			}
		}
		if(this.jumpingTick-->0)
		{
			Vec3d facing = this.getLookVec().scale(0.5);
			this.motionX=facing.xCoord;
			this.motionZ=facing.zCoord;
		}
		if(this.isOnLand())
		{
			if(!this.getPassengers().isEmpty())
			{
				((EntityMob)this.getPassengers().get(0)).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:resistance"),2,4, false,false));
				((EntityMob)this.getPassengers().get(0)).getNavigator().clearPathEntity();
				this.dismountEntity(this.getPassengers().get(0));
			}
			this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
		}
		super.onLivingUpdate();
    }
	
	@Override
	public boolean isMoving()
	{
		return true;
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {}

	private boolean nearShore(int cliffSize)
	{
		if(cliffSize<3)
		{
			BlockPos pos = this.getPosition().offset(this.getHorizontalFacing()).up(cliffSize);
			if(this.worldObj.getBlockState(pos).getMaterial().isSolid() && this.worldObj.getBlockState(pos.up()).getMaterial()==Material.AIR)
				return true;
			else
				return this.nearShore(cliffSize+1);
		}
		return false;
	}
	
	private boolean isOnLand()
	{
		if(this.worldObj.getBlockState(this.getPosition()).getMaterial()!=Material.WATER)
		{
			if(this.worldObj.getBlockState(this.getPosition().down()).getMaterial().isSolid())
				return true;
		}
		return false;
	}

	public boolean shouldDismountInWater(Entity rider)
    {
        return false;
    }
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(!this.getPassengers().isEmpty())
			if(source.getEntity()==this.getPassengers().get(0))
				return false;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void onDeathUpdate() {this.setDead();}

	@Override
	public void onDeath(DamageSource cause) {}
}
