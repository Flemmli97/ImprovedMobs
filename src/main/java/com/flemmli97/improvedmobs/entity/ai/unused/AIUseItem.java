package com.flemmli97.improvedmobs.entity.ai.unused;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraftforge.common.util.FakePlayer;

/**dropped for now*/
public class AIUseItem extends EntityAIBase{

	private EntityLiving living;
    double speedToTarget = 1;
	private int attackCooldown=20;
	private int itemUseCount=20;
	private float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards, useMainHand, hasNoCharge;
    private int strafingTime = -1;
    private ItemStack mainPre;
    private ItemStack offPre;
    private boolean testRightItems, isRightItems;

	public AIUseItem(EntityLiving entity, float maxDistance)
	{
		this.living=entity;
        this.maxAttackDistance = maxDistance * maxDistance;
        this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = living.getAttackTarget();
		if (target == null)
            return false;
		else if(!target.isEntityAlive())
			return false;
        else 
        {
        	ItemStack stackMain = this.living.getHeldItemMainhand();
    		ItemStack stackOff = this.living.getHeldItemOffhand();
        	if(stackMain!=null)
        	{
        		if(mainPre == null||(mainPre.getItem() != stackMain.getItem()))
        		{
        			mainPre = stackMain;
        			testRightItems=true;
        		}
        		else
        			testRightItems=false;
        	}
        	else if(stackOff!=null && stackOff!=offPre)
        	{
        		if(offPre == null||(offPre.getItem() != stackOff.getItem()))
        		{
        			offPre = stackOff;
        			testRightItems=true;
	    		}
	    		else
	    			testRightItems=false;
        	}
        	else 
        		testRightItems=true;
            return this.isHeldingRightItem();
        }
	}

	@Override
	public boolean shouldContinueExecuting()
    {
        return this.shouldExecute();
    }

	@Override
	public void startExecuting()
    {
		
    }

	@Override
	public void resetTask() {
		this.seeTime = 0;
        this.attackTime = -1;
        this.living.resetActiveHand();
	}

	@Override
	public void updateTask() {
		EntityLivingBase target = this.living.getAttackTarget();
		if(target!=null)
		{
			double d0 = this.living.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
            boolean flag = this.living.getEntitySenses().canSee(target);
            boolean flag1 = this.seeTime > 0;

            if (flag != flag1)
            {
                this.seeTime = 0;
            }

            if (flag)
            {
                ++this.seeTime;
            }
            else
            {
                --this.seeTime;
            }

            if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20)
            {
                this.living.getNavigator().clearPath();
                ++this.strafingTime;
            }
            else
            {
                this.living.getNavigator().tryMoveToEntityLiving(target, this.speedToTarget);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20)
            {
                if ((double)this.living.getRNG().nextFloat() < 0.3D)
                {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.living.getRNG().nextFloat() < 0.3D)
                {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1)
            {
                if (d0 > (double)(this.maxAttackDistance * 0.75F))
                {
                    this.strafingBackwards = false;
                }
                else if (d0 < (double)(this.maxAttackDistance * 0.25F))
                {
                    this.strafingBackwards = true;
                }

                this.living.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.living.faceEntity(target, 30.0F, 30.0F);
            }
            else
            {
                this.living.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            }
            FakePlayer fake = FakePlayerHandler.getFakePlayer(living.world, this.living);
        	ItemStack stack = useMainHand ? this.living.getHeldItemMainhand():this.living.getHeldItemOffhand();
        	ItemStack stackCopy = stack;
            if (this.living.isHandActive()||hasNoCharge && stack!=null)
            {
                if (!flag && this.seeTime < -60)
                {
                    this.living.resetActiveHand();
                    fake.resetActiveHand();
            		fake.setDead();
            		hasNoCharge=false;
                }
                else if (flag)
                {
                    int i = this.living.getItemInUseMaxCount();
                    if (i >= this.itemUseCount)
                    {
                    	if(stack.getItem() instanceof ItemBow)
                    	{
                            this.living.resetActiveHand();
                        	FakePlayerHandler.setProperties(fake, living);
                            fake.stopActiveHand();
                    		fake.setDead();
                    		//this.attackEntityWithRangedAttack(this.living, target, ItemBow.getArrowVelocity(i));
                    	}
                    	else if(this.itemUseCount!=0)
                        {
                            this.living.stopActiveHand();
                        	FakePlayerHandler.setProperties(fake, living);
                        	fake.stopActiveHand();
                        	this.syncDataPlayerEntity(fake, living, useMainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                    		fake.setDead();
                        }
                        else if(hasNoCharge)         		
                        {
                            this.living.resetActiveHand();
                        	FakePlayerHandler.setProperties(fake, living);
                        	EnumActionResult res = fake.interactionManager.processRightClick(fake, living.world, useMainHand ?fake.getHeldItemMainhand():fake.getHeldItemOffhand(), useMainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                    		fake.setDead();
                    		hasNoCharge=false;
                    		if((res == EnumActionResult.PASS || res == EnumActionResult.FAIL))
                    			isRightItems=false;
                    	}
                		this.attackTime = this.attackCooldown;
                    }
                }
            }
            else if (--this.attackTime <= 0 && this.seeTime >= -60)
            {
        		this.living.setActiveHand(useMainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            	if(this.itemUseCount!=0 && stackCopy!=null)
            	{            		
            		stackCopy.setCount(1);
            		//EnumActionResult res = fake.interactionManager.processRightClick(fake, living.worldObj, stackCopy, useMainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            		if(net.minecraftforge.common.ForgeHooks.onItemRightClick(fake, useMainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND)==EnumActionResult.FAIL)
            			isRightItems=false;
            	}
            	else
            		hasNoCharge=true;
            }
		}
	}
	
	public boolean isHeldingRightItem()
	{
		ItemStack stackMain = this.living.getHeldItemMainhand();
		ItemStack stackOff = this.living.getHeldItemOffhand();
		if(testRightItems)
		{
			testRightItems=false;
			System.out.println(testRightItems + " and is item " + isRightItems);
			if(living instanceof IRangedAttackMob && !(living instanceof EntitySkeleton))
				return false;
			else if(living instanceof EntityCreeper ||(living instanceof EntitySkeleton && stackMain.getItem() instanceof ItemBow))
				return false;
			else if(stackMain!=null && !(stackMain.getItem() instanceof ItemSword || stackMain.getItem() instanceof ItemTool|| stackMain.getItem() instanceof ItemBlock || stackMain.getItem() instanceof ItemEnderPearl|| stackMain.getItem() instanceof ItemMap))
			{			
				if(!(stackMain.getItem() instanceof ItemBow))
						this.itemUseCount = stackMain.getItem().getMaxItemUseDuration(stackMain);
				else 
					this.itemUseCount=20;
				if(stackMain.getItem() instanceof ItemSplashPotion || stackMain.getItem() instanceof ItemLingeringPotion)
						this.itemUseCount = 0;
				isRightItems = true;
				useMainHand=true;
				return true;
			}
			else if(stackOff!=null && !(stackOff.getItem() instanceof ItemBlock  || stackOff.getItem() instanceof ItemEnderPearl|| stackOff.getItem() instanceof ItemMap))
			{
				if(!(stackOff.getItem() instanceof ItemBow))
					this.itemUseCount = stackOff.getItem().getMaxItemUseDuration(stackOff);
				else 
					this.itemUseCount=20;
				if(stackOff.getItem() instanceof ItemSplashPotion || stackOff.getItem() instanceof ItemLingeringPotion)
					this.itemUseCount = 0;
				isRightItems = true;
				useMainHand=false;
				return true;
			}
			return false;
		}
		return this.isRightItems;
	}

	public void syncDataPlayerEntity(FakePlayer player, EntityLiving living, EnumHand hand)
	{
		ItemStack stack = living.getHeldItem(hand);
			stack.shrink(1);;
			if(stack.getCount()<=0)
				stack=ItemStack.EMPTY;
			living.setHeldItem(hand, stack);
	}
	
	public void attackEntityWithRangedAttack(EntityLivingBase theEntity, EntityLivingBase target, float distanceFactor)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(theEntity.world, theEntity);
        double d0 = target.posX - theEntity.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entitytippedarrow.posY;
        if(target.height <0.5)
        	d1=target.getEntityBoundingBox().minY - entitytippedarrow.posY;
        double d2 = target.posZ - theEntity.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        entitytippedarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - theEntity.world.getDifficulty().getDifficultyId() * 4));
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, theEntity);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, theEntity);
        DifficultyInstance difficultyinstance = theEntity.world.getDifficultyForLocation(new BlockPos(theEntity));
        entitytippedarrow.setDamage((double)(distanceFactor * 2.0F) + theEntity.world.rand.nextGaussian() * 0.25D + (double)((float)theEntity.world.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0)
        {
            entitytippedarrow.setDamage(entitytippedarrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0)
        {
            entitytippedarrow.setKnockbackStrength(j);
        }

        boolean flag = theEntity.isBurning() && difficultyinstance.isHarderThan(3) && theEntity.world.rand.nextBoolean();
        flag = flag || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, theEntity) > 0;

        if (flag)
        {
            entitytippedarrow.setFire(100);
        }

        ItemStack itemstack = theEntity.getHeldItem(EnumHand.OFF_HAND);

        if (itemstack != null && itemstack.getItem() == Items.TIPPED_ARROW)
        {
            entitytippedarrow.setPotionEffect(itemstack);
        }

        theEntity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (theEntity.getRNG().nextFloat() * 0.4F + 0.8F));
        theEntity.world.spawnEntity(entitytippedarrow);
    }
}
