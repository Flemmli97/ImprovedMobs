package com.flemmli97.improvedmobs.handler.helper;

import java.util.List;

import com.flemmli97.improvedmobs.entity.EntityMobSplash;
import com.flemmli97.improvedmobs.entity.EntitySnowBallNew;
import com.flemmli97.improvedmobs.entity.EntityTntNew;
import com.flemmli97.improvedmobs.handler.ItemType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class AIUseHelper {
	
	private final static String[] potionEffects = new String[] {"minecraft:regeneration", "minecraft:speed", "minecraft:strength", "minecraft:invisibility", "minecraft:resistance", "minecraft:fire_resistance"};

	//TODO building, stone;
	//TODO fishing rod
	
	public static ItemType isItemApplicable(EntityLiving living)
	{
		ItemType type = ItemType.NOTHING;
		ItemStack stackMain = living.getHeldItemMainhand();
		ItemStack stackOff = living.getHeldItemOffhand();
		boolean flagMain = false;
		boolean flagOff = false;

		if(stackMain !=null)
		{
			if(stackMain.getItem() instanceof ItemSplashPotion && AIUseHelper.isDamagePotion(stackMain))
			{
				type = ItemType.STRAFINGITEM;
				flagMain=true;
			}
			else if(stackMain.getItem() == Items.ENDER_PEARL ||stackMain.getItem() == Items.LAVA_BUCKET || stackMain.getItem() == Items.BUCKET 
					|| stackMain.getItem() == Items.FLINT_AND_STEEL ||stackMain.getItem()==Items.ENCHANTED_BOOK/*|| stackMain.getItem() instanceof ItemFishingRod*/)
			{
				type = ItemType.NONSTRAFINGITEM;
				flagMain=true;
			}
			
			else if(stackMain.getItem() == Items.SNOWBALL || stackMain.getItem() ==ItemBlock.getItemFromBlock(Blocks.TNT))
			{
				type = ItemType.STRAFINGITEM;
				flagMain=true;
			}
			else if(stackMain.getItem() == Items.BOW)
			{
				type = ItemType.BOW;
				flagMain=true;
			}
			if(flagMain)
			{
				type.setHand(EnumHand.MAIN_HAND);
				type.setItem(stackMain);
			}
		}
		if(stackOff != null && !flagMain)
		{
			if(stackOff.getItem() instanceof ItemSplashPotion && AIUseHelper.isDamagePotion(stackOff))
			{
				type=ItemType.STRAFINGITEM;
				flagOff=true;
			}
			else if(stackOff.getItem() == Items.ENDER_PEARL ||stackOff.getItem() == Items.LAVA_BUCKET || stackOff.getItem() == Items.BUCKET
					|| stackOff.getItem() == Items.FLINT_AND_STEEL || stackOff.getItem() instanceof ItemShield ||stackOff.getItem()==Items.ENCHANTED_BOOK/*|| stackMain.getItem() instanceof ItemFishingRod*/)
			{
				type = ItemType.NONSTRAFINGITEM;
				flagOff = true;
			}
			else if(stackOff.getItem() == Items.SNOWBALL || stackOff.getItem() == ItemBlock.getItemFromBlock(Blocks.TNT))
			{
				type = ItemType.STRAFINGITEM;
				flagOff=true;
			}
			else if(stackOff.getItem() == Items.BOW)
			{
				if(stackMain !=null && stackMain.getItem() instanceof ItemArrow)
				{
					flagOff=false;
					type=ItemType.BOW;
					living.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stackOff.copy());
					living.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stackMain.copy());
					type.setHand(EnumHand.MAIN_HAND);
					type.setItem(stackOff);
				}
				else
				{
					type = ItemType.BOW;
					flagOff=true;
				}
			}
			if(flagOff)
			{
				type.setHand(EnumHand.OFF_HAND);
				type.setItem(stackOff);
			}
		}
		return type;
	}

	public static void chooseAttack(EntityLiving theEntity, EntityLivingBase target)
	{
		ItemType type = AIUseHelper.isItemApplicable(theEntity);
		double dis = theEntity.getPositionVector().distanceTo(target.getPositionVector());
		if(type.getItem() == Items.SNOWBALL)
		{
			theEntity.worldObj.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.worldObj.rand.nextFloat() * 0.4F + 0.8F));

	        if (!theEntity.worldObj.isRemote)
	        {
	            EntitySnowBallNew entitysnowball = new EntitySnowBallNew(theEntity.worldObj, theEntity);
	            entitysnowball.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, 0.0F, 1.5F, 1.0F);
	            theEntity.worldObj.spawnEntityInWorld(entitysnowball);
	        }		
	    }
		else if(type.getItem() == Items.ENDER_PEARL)
		{
			if(dis > 16.0)
    			{
				theEntity.worldObj.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.worldObj.rand.nextFloat() * 0.4F + 0.8F));
		        if (!theEntity.worldObj.isRemote)
		        {		        		
		        		Vec3d v1= theEntity.getPositionVector().subtract(target.getPositionVector()).normalize().scale(16);
		        		double x=0;
		        		double y=0;
		        		double z=0;
		        		if(theEntity.getPositionVector().subtract(target.getPositionVector()).lengthVector() > 16)
		        		{
		        			x=v1.xCoord;
		        			y=v1.yCoord;
		        			z=v1.zCoord;
		        		}
		        		EntityEnderPearl pearl = new EntityEnderPearl(theEntity.worldObj, theEntity);
			        setHeadingToPosition(pearl, target.posX-x, target.posY-y, target.posZ-z, 1.5F, 3.0F);
		        		theEntity.worldObj.spawnEntityInWorld(pearl);
	        		}
		   }		
	    }
		else if(type.getItem() instanceof ItemBucket)
		{
			if(type.getItem() == Items.LAVA_BUCKET)
			{
				if(dis < 8 && AIUseHelper.tryPlaceLava(theEntity.worldObj, new BlockPos(target.posX-2+theEntity.worldObj.rand.nextInt(4),target.posY-1+theEntity.worldObj.rand.nextInt(2),target.posZ-2+theEntity.worldObj.rand.nextInt(4))))
				{
					theEntity.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:fire_resistance"), 240, 1, true, false));
					//theEntity.setItemStackToSlot(type.getHand() == EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, new ItemStack(Items.BUCKET));
				}
			}
			else if (type.getItem() == Items.BUCKET)
			{
				for(int i = -4;i<=4;i++)
					for(int j = -4;j<= 4; j++)
						for(int k = -1;k<=2;k++)
						{
							BlockPos pos = new BlockPos(theEntity).add(i, k, j);
							IBlockState state = theEntity.worldObj.getBlockState(pos);
							Block block = state.getBlock();
							if(block == Blocks.LAVA && ((Integer)state.getValue(BlockLiquid.LEVEL)).intValue() == 0)
							{
								theEntity.worldObj.setBlockToAir(pos);
								theEntity.setItemStackToSlot(type.getHand() == EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, new ItemStack(Items.LAVA_BUCKET));
								return;
							}
						}
			}
	    }
		else if(type.getItem() == Items.FLINT_AND_STEEL)
		{
			if(dis < theEntity.width + target.width + 0.5)
			{
				if(!target.isBurning())
				{
					target.setFire(4);
				}
			}
		}
		else if(type.getItem() instanceof ItemSplashPotion && AIUseHelper.isDamagePotion(type.getStack()))//to improve
		{				
			theEntity.worldObj.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.worldObj.rand.nextFloat() * 0.4F + 0.8F));
	        if (!theEntity.worldObj.isRemote)
	        {
	        		EntityMobSplash entitypotion = new EntityMobSplash(theEntity.worldObj, theEntity, type.getStack());
	        		entitypotion.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, -30.0F,  0.2F+(float)(dis*0.05), 1.2F);
	        		theEntity.worldObj.spawnEntityInWorld(entitypotion);
			}
		}
		else if(type.getItem() == ItemBlock.getItemFromBlock(Blocks.TNT))
		{
	        if (!theEntity.worldObj.isRemote)
	        {
				EntityTntNew tnt = new EntityTntNew(theEntity.worldObj, theEntity.posX, theEntity.posY, theEntity.posZ, theEntity);
				tnt.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, -20.0F, 0.2F+(float)(dis*0.05), 1.0F);
				theEntity.worldObj.spawnEntityInWorld(tnt);
	        }
		}
		else if(type.getItem()==Items.ENCHANTED_BOOK)
		{
			if (!theEntity.worldObj.isRemote)
	        {
				List<Entity> nearby = theEntity.worldObj.getEntitiesWithinAABBExcludingEntity(theEntity, theEntity.getEntityBoundingBox().expandXyz(8.0D));
				if(!nearby.isEmpty())
				{
					for(int i = 0; i < nearby.size();i++)
					{
						Entity entity = nearby.get(theEntity.worldObj.rand.nextInt(nearby.size()));
						if(entity instanceof EntityMob && entity!=theEntity.getAttackTarget())
						{
							EntityMob mob = (EntityMob) entity;
							mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(potionEffects[mob.worldObj.rand.nextInt(6)]), 3600, 1));
							theEntity.worldObj.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.NEUTRAL, 2F, 1.0F);
							return;
						}
					}
				}
	        }
		}
		/*else if(type.getItem() instanceof ItemFishingRod)
		{
			theEntity.worldObj.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ,SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.worldObj.rand.nextFloat() * 0.4F + 0.8F));
			if (!theEntity.worldObj.isRemote)
	        {
	        		EntityPotion entitypotion = new EntityPotion(theEntity.worldObj, theEntity, type.getStack());
	        		entitypotion.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, -30.0F,  0.2F+(float)(dis*0.05), 1.2F);
	        		theEntity.worldObj.spawnEntityInWorld(new EntityFishHook(theEntity.worldObj, theEntity));
			}
		}*/
	}
	
	private static void setHeadingToPosition(EntityThrowable e ,double x, double y, double z, float velocity, float inaccuracy)
    {
		Vec3d dir = new Vec3d (x-e.posX, y - e.posY, z-e.posZ).scale(1/velocity);
		e.setThrowableHeading(dir.xCoord, dir.yCoord, dir.zCoord, velocity, inaccuracy);
    }
	
	public static void attackWithArrows(EntityArrow arrow, EntityLivingBase theEntity, EntityLivingBase target, float distanceFactor)
    {
        double d0 = target.posX - theEntity.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - arrow.posY;
        if(target.height <0.5)
        	d1=target.getEntityBoundingBox().minY - arrow.posY;
        double d2 = target.posZ - theEntity.posZ;
        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        arrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - theEntity.worldObj.getDifficulty().getDifficultyId() * 4));
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, theEntity);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, theEntity);
        arrow.setDamage((double)(distanceFactor * 2.0F) + theEntity.worldObj.rand.nextGaussian() * 0.25D + (double)((float)theEntity.worldObj.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0)
        {
            arrow.setDamage(arrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0)
        {
            arrow.setKnockbackStrength(j);
        }

        boolean flag = theEntity.isBurning() && theEntity.worldObj.getDifficulty() == EnumDifficulty.HARD && theEntity.worldObj.rand.nextBoolean();
        flag = flag || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, theEntity) > 0;

        if (flag)
        {
            arrow.setFire(100);
        }

        ItemStack itemstack = theEntity.getHeldItem(EnumHand.OFF_HAND);

        if (itemstack != null && itemstack.getItem() == Items.TIPPED_ARROW && arrow instanceof EntityTippedArrow)
        {
        		EntityTippedArrow tippedArrow = (EntityTippedArrow) arrow;;
        		tippedArrow.setPotionEffect(itemstack);
        }

        theEntity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (theEntity.getRNG().nextFloat() * 0.4F + 0.8F));
        theEntity.worldObj.spawnEntityInWorld(arrow);
    }
	
	private static boolean tryPlaceLava(World worldIn, BlockPos posIn)
	{
		IBlockState iblockstate = worldIn.getBlockState(posIn);
        Material material = iblockstate.getMaterial();
        boolean flag = !material.isSolid();
        boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

        if (!worldIn.isAirBlock(posIn) && !flag && !flag1)
        {
            return false;
        }
        else
        {
        		if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid())
            {
                worldIn.destroyBlock(posIn, true);
            }

            worldIn.playSound(null, posIn, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
            worldIn.setBlockState(posIn, Blocks.FLOWING_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL, 1), 11);
        		return true;
        }
	}
	
	private static boolean isDamagePotion(ItemStack stack)
	{
		List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
		if(effects.size()==1)
		{
			if(effects.get(0).getPotion() == Potion.getPotionFromResourceLocation("minecraft:instant_damage"))
				return true;
		}
		return false;
	}
}
