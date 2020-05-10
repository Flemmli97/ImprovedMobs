package com.flemmli97.improvedmobs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySnowBallNew extends EntitySnowball {

	public EntitySnowBallNew(World worldIn) {
		super(worldIn);
	}

	public EntitySnowBallNew(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
	}

	public EntitySnowBallNew(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if(result.entityHit != this.getThrower())
			if(result.entityHit != null){
				float i = 0.01F;

				if(result.entityHit instanceof EntityBlaze){
					i = 3;
				}

				result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), i);
			}

		for(int j = 0; j < 8; ++j){
			this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		if(!this.world.isRemote){
			this.setDead();
		}
	}

}
