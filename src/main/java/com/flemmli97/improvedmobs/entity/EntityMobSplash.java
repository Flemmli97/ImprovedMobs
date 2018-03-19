package com.flemmli97.improvedmobs.entity;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityMobSplash extends EntityPotion{

	public EntityMobSplash(World worldIn) {
		super(worldIn);
	}
	
    public EntityMobSplash(World worldIn, EntityLivingBase thrower, ItemStack potionDamageIn)
    {
        super(worldIn, thrower, potionDamageIn);
    }

    public EntityMobSplash(World worldIn, double x, double y, double z, ItemStack potionDamageIn)
    {
        super(worldIn, x, y, z, potionDamageIn);
    }

	@Override
	protected void onImpact(RayTraceResult result) {
		if (!this.world.isRemote)
        {
            ItemStack itemstack = this.getPotion();
            PotionType potiontype = PotionUtils.getPotionFromItem(itemstack);
            List<PotionEffect> list = PotionUtils.getEffectsFromStack(itemstack);
            if (!list.isEmpty())
            {
            		this.applySplashPotion(result, list);
            }

            int i = potiontype.hasInstantEffect() ? 2007 : 2002;
            this.world.playEvent(i, new BlockPos(this), PotionUtils.getColor(itemstack));
            this.setDead();
        }
	}

	private void applySplashPotion(RayTraceResult result, List<PotionEffect> list) {
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityLivingBase> listEntity = this.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

        if (!listEntity.isEmpty())
        {
            for (EntityLivingBase entitylivingbase : listEntity)
            {
                if (entitylivingbase.canBeHitWithPotion() && this.getThrower()!=null && this.getThrower() instanceof EntityLiving
                		&& ((EntityLiving) this.getThrower()).getAttackTarget() ==entitylivingbase)
                {
                    double d0 = this.getDistanceSq(entitylivingbase);

                    if (d0 < 16.0D)
                    {
                        double d1 = 1.0D - Math.sqrt(d0) / 4.0D;

                        if (entitylivingbase == result.entityHit)
                        {
                            d1 = 1.0D;
                        }

                        for (PotionEffect potioneffect : list)
                        {
                            Potion potion = potioneffect.getPotion();

                            if (potion.isInstant())
                            {
                                potion.affectEntity(this, this.getThrower(), entitylivingbase, potioneffect.getAmplifier(), d1);
                            }
                            else
                            {
                                int i = (int)(d1 * (double)potioneffect.getDuration() + 0.5D);

                                if (i > 20)
                                {
                                    entitylivingbase.addPotionEffect(new PotionEffect(potion, i, potioneffect.getAmplifier(), potioneffect.getIsAmbient(), potioneffect.doesShowParticles()));
                                }
                            }
                        }
                    }
                }
            }
        }		
	}

	
}
