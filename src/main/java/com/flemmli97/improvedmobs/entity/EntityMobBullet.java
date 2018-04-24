package com.flemmli97.improvedmobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMobBullet extends EntityShulkerBullet{

	private EntityLiving ownerNew;
    public EntityMobBullet(World worldIn)
    {
        super(worldIn);
    }

    @SideOnly(Side.CLIENT)
    public EntityMobBullet(World worldIn, double x, double y, double z, double motionXIn, double motionYIn, double motionZIn)
    {
    		super(worldIn, x, y, z, motionXIn, motionYIn, motionZIn);
    }

    public EntityMobBullet(World worldIn, EntityLiving ownerIn, Entity targetIn, EnumFacing.Axis facing)
    {
    		super(worldIn, ownerIn, targetIn, facing);
    		this.ownerNew = ownerIn;
    }

    @Override
	protected void bulletHit(RayTraceResult result) {
		if (result.entityHit == null)
        {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 2, 0.2D, 0.2D, 0.2D, 0.0D, new int[0]);
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
            this.setDead();
        }
        else
        {
        		if(result.entityHit == this.ownerNew.getAttackTarget())
        		{
	            boolean flag = result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.ownerNew).setProjectile(), 4.0F);
	
	            if (flag)
	            {
	                this.applyEnchantments(this.ownerNew, result.entityHit);
	
	                if (result.entityHit instanceof EntityLivingBase)
	                {
	                    ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 100));
	                }
	            }
	            this.setDead();
        		}
        }
	}
}
