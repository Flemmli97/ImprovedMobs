 package com.flemmli97.improvedmobs.entity.ai;

import com.flemmli97.improvedmobs.entity.EntityGuardianBoat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIRideBoat extends EntityAIBase{

	EntityLiving living;	
	int wait = 0;
	
	public EntityAIRideBoat(EntityLiving living)
	{
		this.living=living;
	}
	
	@Override
	public boolean shouldExecute() {
		if(living.isInWater() && !living.isRiding() && living.getAttackTarget()!=null)
		{
			if(this.wait==60)
				return true;
			if(this.wait<60)
				this.wait++;
			else
				this.wait=0;
		}
		return false;
	}
	
	@Override
	public void startExecuting() {
		EntityGuardianBoat boat = new EntityGuardianBoat(this.living.world);
		boat.setLocationAndAngles(this.living.posX, this.living.posY, this.living.posZ, this.living.rotationYaw, this.living.rotationPitch);
		this.living.world.spawnEntity(boat);
		this.living.startRiding(boat);
		this.wait=0;
	}
}
