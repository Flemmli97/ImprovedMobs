package com.flemmli97.improvedmobs.handler.helper;

import java.util.List;

import com.flemmli97.improvedmobs.entity.EntityMobBullet;
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
import net.minecraft.entity.projectile.EntityEvokerFangs;
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

	//TODO building, stone, block;
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
					|| stackMain.getItem() == Items.FLINT_AND_STEEL)
			{
				type = ItemType.NONSTRAFINGITEM;
				flagMain=true;
			}
			
			else if(stackMain.getItem() == Items.SNOWBALL || stackMain.getItem() ==ItemBlock.getItemFromBlock(Blocks.TNT)||stackMain.getItem()==Items.ENCHANTED_BOOK)
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
					|| stackOff.getItem() == Items.FLINT_AND_STEEL || stackOff.getItem() instanceof ItemShield)
			{
				type = ItemType.NONSTRAFINGITEM;
				flagOff = true;
			}
			else if(stackOff.getItem() == Items.SNOWBALL || stackOff.getItem() == ItemBlock.getItemFromBlock(Blocks.TNT)||stackOff.getItem()==Items.ENCHANTED_BOOK)
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
			theEntity.world.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.world.rand.nextFloat() * 0.4F + 0.8F));

	        if (!theEntity.world.isRemote)
	        {
	            EntitySnowBallNew entitysnowball = new EntitySnowBallNew(theEntity.world, theEntity);
	            entitysnowball.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, 0.0F, 1.5F, 1.0F);
	            theEntity.world.spawnEntity(entitysnowball);
	        }		
	    }
		else if(type.getItem() == Items.ENDER_PEARL)
		{
			if(dis > 16.0)
    			{
				theEntity.world.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.world.rand.nextFloat() * 0.4F + 0.8F));
		        if (!theEntity.world.isRemote)
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
		        		EntityEnderPearl pearl = new EntityEnderPearl(theEntity.world, theEntity);
			        setHeadingToPosition(pearl, target.posX-x, target.posY-y, target.posZ-z, 1.5F, 3.0F);
		        		theEntity.world.spawnEntity(pearl);
	        		}
		   }		
	    }
		else if(type.getItem() instanceof ItemBucket)
		{
			if(type.getItem() == Items.LAVA_BUCKET)
			{
				if(dis < 8 && AIUseHelper.tryPlaceLava(theEntity.world, new BlockPos(target.posX-2+theEntity.world.rand.nextInt(4),target.posY-1+theEntity.world.rand.nextInt(2),target.posZ-2+theEntity.world.rand.nextInt(4))))
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
							IBlockState state = theEntity.world.getBlockState(pos);
							Block block = state.getBlock();
							if(block == Blocks.LAVA && ((Integer)state.getValue(BlockLiquid.LEVEL)).intValue() == 0)
							{
								theEntity.world.setBlockToAir(pos);
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
			theEntity.world.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (theEntity.world.rand.nextFloat() * 0.4F + 0.8F));
	        if (!theEntity.world.isRemote)
	        {
	        		EntityMobSplash entitypotion = new EntityMobSplash(theEntity.world, theEntity, type.getStack());
	        		entitypotion.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, -30.0F,  0.2F+(float)(dis*0.05), 1.2F);
	        		theEntity.world.spawnEntity(entitypotion);
			}
		}
		else if(type.getItem() == ItemBlock.getItemFromBlock(Blocks.TNT))
		{
	        if (!theEntity.world.isRemote)
	        {
				EntityTntNew tnt = new EntityTntNew(theEntity.world, theEntity.posX, theEntity.posY, theEntity.posZ, theEntity);
				tnt.setHeadingFromThrower(theEntity, theEntity.rotationPitch, theEntity.rotationYaw, -20.0F, 0.2F+(float)(dis*0.05), 1.0F);
				theEntity.world.spawnEntity(tnt);
	        }
		}
		else if(type.getItem()==Items.ENCHANTED_BOOK)
		{
			if (!theEntity.world.isRemote)
	        {
				List<Entity> nearby = theEntity.world.getEntitiesWithinAABBExcludingEntity(theEntity, theEntity.getEntityBoundingBox().expandXyz(8.0D));
				List<Entity> nearTarget = theEntity.world.getEntitiesWithinAABBExcludingEntity(theEntity.getAttackTarget(), theEntity.getAttackTarget().getEntityBoundingBox().expandXyz(2.0D));
				if(nearby.isEmpty() || nearby.size()==1 && nearby.get(0) ==theEntity.getAttackTarget() || theEntity.world.rand.nextInt(3)<=1)
				{
					if(nearTarget.isEmpty())
						for(int x = -1;x<=1;x++)
							for(int z=-1;z<=1;z++)
							{
								if(x==0 || z==0)
								{
									EntityEvokerFangs fang = new EntityEvokerFangs(theEntity.world, target.posX+x+target.motionX, target.posY, target.posZ+z+target.motionZ, 0, 5, theEntity);
									theEntity.world.spawnEntity(fang);
								}
							}
					else
					{
                        EntityMobBullet entityBullet = new EntityMobBullet(theEntity.world, theEntity, theEntity.getAttackTarget(), theEntity.getHorizontalFacing().getAxis());
                        theEntity.world.spawnEntity(entityBullet);
					}
				}
				else if(theEntity.world.rand.nextInt(4)==0)
				{
					for(int i = 0; i < nearby.size();i++)
					{
						Entity entity = nearby.get(theEntity.world.rand.nextInt(nearby.size()));
						if(entity instanceof EntityMob &&entity!=theEntity.getAttackTarget())
						{
							EntityMob mob = (EntityMob) entity;
							mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(potionEffects[mob.world.rand.nextInt(6)]), 3600, 1));
							theEntity.world.playSound((EntityPlayer)null, theEntity.posX, theEntity.posY, theEntity.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.NEUTRAL, 2F, 1.0F);
							return;
						}
					}
				}
	        }
		}
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
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - theEntity.world.getDifficulty().getDifficultyId() * 4));
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, theEntity);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, theEntity);
        arrow.setDamage((double)(distanceFactor * 2.0F) + theEntity.world.rand.nextGaussian() * 0.25D + (double)((float)theEntity.world.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0)
        {
            arrow.setDamage(arrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0)
        {
            arrow.setKnockbackStrength(j);
        }

        boolean flag = theEntity.isBurning() && theEntity.world.getDifficulty() == EnumDifficulty.HARD && theEntity.world.rand.nextBoolean();
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
        theEntity.world.spawnEntity(arrow);
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
