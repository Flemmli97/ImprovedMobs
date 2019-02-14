package com.flemmli97.improvedmobs.handler.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.silvercatcher.reforged.api.ReforgedAdditions;
import org.silvercatcher.reforged.entities.EntityBulletBlunderbuss;
import org.silvercatcher.reforged.entities.EntityBulletMusket;
import org.silvercatcher.reforged.entities.EntityCrossbowBolt;
import org.silvercatcher.reforged.entities.EntityDart;
import org.silvercatcher.reforged.entities.EntityJavelin;
import org.silvercatcher.reforged.items.others.ItemDart;
import org.silvercatcher.reforged.items.weapons.ItemBlowGun;
import org.silvercatcher.reforged.items.weapons.ItemCrossbow;
import org.silvercatcher.reforged.items.weapons.ItemJavelin;
import org.silvercatcher.reforged.items.weapons.ItemMusket;
import org.silvercatcher.reforged.util.Helpers;

import com.flemmli97.improvedmobs.entity.EntityMobBullet;
import com.flemmli97.improvedmobs.entity.EntityMobSplash;
import com.flemmli97.improvedmobs.entity.EntitySnowBallNew;
import com.flemmli97.improvedmobs.entity.EntityTntNew;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.entity.projectile.EntityEvokerFangs;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
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
	
	private static void setHeadingToPosition(EntityThrowable e ,double x, double y, double z, float velocity, float inaccuracy)
    {
		Vec3d dir = new Vec3d (x-e.posX, y - e.posY, z-e.posZ).scale(1/velocity);
		e.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }
	
	public static void attackWithArrows(EntityArrow arrow, EntityLivingBase theEntity, EntityLivingBase target, float distanceFactor)
    {
        double d0 = target.posX - theEntity.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - arrow.posY;
        if(target.height <0.5)
        	d1=target.getEntityBoundingBox().minY - arrow.posY;
        double d2 = target.posZ - theEntity.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - theEntity.world.getDifficulty().getDifficultyId() * 4));
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
	
	private static final Map<Class<? extends Item>, ItemAI> clssMap = Maps.newHashMap();
	private static final Map<Item, ItemAI> itemMap = Maps.newHashMap();

	
	
	public static interface ItemAI
	{
		public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand);
		
		public int cooldown();
		
		public ItemType type();
		
		public Hand prefHand();
		
		public default boolean useHand() {return false;}
		
		public default int maxUseCount() {return 20;}
	}
	
	@Nullable
	public static Pair<ItemAI,EnumHand> getAI(EntityLiving entity)
	{
		ItemStack heldMain = entity.getHeldItemMainhand();
		ItemStack heldOff = entity.getHeldItemOffhand();
		if(heldMain.getItem() instanceof ItemArrow && heldOff.getItem() instanceof ItemBow)
		{
			entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, heldOff.copy());
			entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, heldMain.copy());
			heldMain = entity.getHeldItemMainhand();
			heldOff = entity.getHeldItemOffhand();
		}
		ItemAI ai = null;
		EnumHand hand = EnumHand.MAIN_HAND;
		ai = itemMap.get(heldMain.getItem());
		if(ai==null)
		{
			ai = itemMap.get(heldOff.getItem());
			hand = EnumHand.OFF_HAND;
		}
		
		if(ai==null)
			for(Entry<Class<? extends Item>, ItemAI> entry : clssMap.entrySet())
			{
				if(entry.getKey().equals(heldMain.getItem().getClass().getSuperclass()) && entry.getValue().prefHand()!=Hand.OFF)
				{
					ai=entry.getValue();
					hand = EnumHand.MAIN_HAND;
					if(ai!=null)
						break;
				}
				if(entry.getKey().equals(heldMain.getItem().getClass().getSuperclass()) && entry.getValue().prefHand()!=Hand.MAIN)
				{
					ai=entry.getValue();
					hand = EnumHand.OFF_HAND;
					if(ai!=null)
						break;
				}
				if(entry.getKey().isAssignableFrom(heldMain.getItem().getClass()) && entry.getValue().prefHand()!=Hand.OFF)
				{
					ai=entry.getValue();
					hand = EnumHand.MAIN_HAND;
					if(ai!=null)
						break;
				}
				if(entry.getKey().isAssignableFrom(heldOff.getItem().getClass()) && entry.getValue().prefHand()!=Hand.MAIN)
				{
					ai=entry.getValue();
					hand = EnumHand.OFF_HAND;
					if(ai!=null)
						break;
				}
			}
		return Pair.of(ai,hand);
	}
	
	public static enum ItemType
	{
		NONSTRAFINGITEM,
		STRAFINGITEM;
	}
	
	public static enum Hand {

		MAIN,
		OFF,
		BOTH;
	}
	
	//Boomerang doesnt work. will crash
	public static void initReforgedStuff()
	{
		itemMap.put(ReforgedAdditions.BLUNDERBUSS, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				Helpers.playSound(entity.world, entity, "shotgun_shoot", 1, 1);
				for (int i = 1; i < 12; i++) {
					entity.world.spawnEntity(new EntityBulletBlunderbuss(entity.world, entity, entity.getHeldItem(hand)));
				}
			}
			@Override
			public int cooldown() {return 60;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		clssMap.put(ItemMusket.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				Helpers.playSound(entity.world, entity, "musket_shoot", 1, 1);
				if(!entity.world.isRemote) {
					entity.world.spawnEntity(new EntityBulletMusket(entity.world, entity, entity.getHeldItem(hand)));
				}
			}
			@Override
			public int cooldown() {return 45;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		clssMap.put(ItemBlowGun.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				if(!entity.world.isRemote)
				{
					ItemStack stack = entity.getHeldItemOffhand();
					EntityDart dart = null;
					if(stack.getItem() instanceof ItemDart)
					{
						dart = new EntityDart(entity.world, entity, stack.copy());
					}
					else
						dart = new EntityDart(entity.world, entity, new ItemStack(ReforgedAdditions.DART_NORMAL));
					entity.world.spawnEntity(dart);
				}
			}
			@Override
			public int cooldown() {return 55;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.MAIN;}});
		clssMap.put(ItemCrossbow.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				Helpers.playSound(entity.world, entity, "crossbow_shoot", 1, 1);
				if(!entity.world.isRemote) {
					EntityCrossbowBolt a = new EntityCrossbowBolt(entity.world, entity);
					a.setAim(entity, entity.rotationPitch, entity.rotationYaw, 0.0F, ItemBow.getArrowVelocity(40) * 3.0F, 1.0F);
					a.pickupStatus = PickupStatus.getByOrdinal(new Random().nextInt(2));
					a.setDamage(8.0D);
					entity.world.spawnEntity(a);
				}
			}
			@Override
			public int cooldown() {return 45;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		clssMap.put(ItemJavelin.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ARROW_SHOOT,
						SoundCategory.MASTER, 0.5F, 0.4F / (entity.getRNG().nextFloat() * 0.4F + 0.8F));
				if(!entity.world.isRemote) {
					ItemStack stack = entity.getHeldItem(hand);
					entity.world.spawnEntity(
							new EntityJavelin(entity.world, entity, stack, stack.getMaxItemUseDuration() - entity.getItemInUseCount()));
				}
			}
			@Override
			public int cooldown() {return 35;}
			@Override
			public ItemType type() {return ItemType.NONSTRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
	}
	
	static
	{
		clssMap.put(ItemSplashPotion.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				ItemStack stack = entity.getHeldItem(hand);
				if(AIUseHelper.isDamagePotion(stack))
				{				
					double dis = entity.getPositionVector().distanceTo(target.getPositionVector());
					entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
			        if (!entity.world.isRemote)
			        {
			        		EntityMobSplash entitypotion = new EntityMobSplash(entity.world, entity, stack);
			        		entitypotion.shoot(entity, entity.rotationPitch, entity.rotationYaw, -30.0F,  0.2F+(float)(dis*0.05), 1.2F);
			        		entity.world.spawnEntity(entitypotion);
					}
				}
			}
			@Override
			public int cooldown() {return 85;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(Items.SNOWBALL, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
		        if (!entity.world.isRemote)
		        {
		            EntitySnowBallNew entitysnowball = new EntitySnowBallNew(entity.world, entity);
		            entitysnowball.shoot(entity, entity.rotationPitch, entity.rotationYaw, 0.0F, 1.5F, 1.0F);
		            entity.world.spawnEntity(entitysnowball);
		        }
			}
			@Override
			public int cooldown() {return 25;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(Items.ENDER_PEARL, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				double dis = entity.getPositionVector().distanceTo(target.getPositionVector());
				if(dis > 16.0)
    			{
					entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.world.rand.nextFloat() * 0.4F + 0.8F));
			        if (!entity.world.isRemote)
			        {		        		
		        		Vec3d v1= entity.getPositionVector().subtract(target.getPositionVector()).normalize().scale(16);
		        		double x=0;
		        		double y=0;
		        		double z=0;
		        		if(entity.getPositionVector().subtract(target.getPositionVector()).lengthVector() > 16)
		        		{
		        			x=v1.x;
		        			y=v1.y;
		        			z=v1.z;
		        		}
		        		EntityEnderPearl pearl = new EntityEnderPearl(entity.world, entity);
		        		setHeadingToPosition(pearl, target.posX-x, target.posY-y, target.posZ-z, 1.5F, 3.0F);
		        		entity.world.spawnEntity(pearl);
	        		}
			   }
			}
			@Override
			public int cooldown() {return 35;}
			@Override
			public ItemType type() {return ItemType.NONSTRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(Items.LAVA_BUCKET, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				double dis = entity.getPositionVector().distanceTo(target.getPositionVector());
				if(dis < 8 && AIUseHelper.tryPlaceLava(entity.world, new BlockPos(target.posX-2+entity.world.rand.nextInt(4),target.posY-1+entity.world.rand.nextInt(2),target.posZ-2+entity.world.rand.nextInt(4))))
				{
					entity.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:fire_resistance"), 240, 1, true, false));
				}
			}
			@Override
			public int cooldown() {return 80;}
			@Override
			public ItemType type() {return ItemType.NONSTRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(Items.FLINT_AND_STEEL, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				double dis = entity.getPositionVector().distanceTo(target.getPositionVector());
				if(dis < entity.width + target.width + 0.5 && !target.isBurning())
				{
					target.setFire(4);
				}
			}
			@Override
			public int cooldown() {return 25;}
			@Override
			public ItemType type() {return ItemType.NONSTRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(ItemBlock.getItemFromBlock(Blocks.TNT), new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				double dis = entity.getPositionVector().distanceTo(target.getPositionVector());
				if (!entity.world.isRemote)
		        {
					EntityTntNew tnt = new EntityTntNew(entity.world, entity.posX, entity.posY, entity.posZ, entity);
					tnt.setHeadingFromThrower(entity, entity.rotationPitch, entity.rotationYaw, -20.0F, 0.2F+(float)(dis*0.05), 1.0F);
					entity.world.spawnEntity(tnt);
		        }
			}
			@Override
			public int cooldown() {return 65;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		itemMap.put(Items.ENCHANTED_BOOK, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
				if (!entity.world.isRemote)
		        {
					List<Entity> nearby = entity.world.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().grow(8.0D));
					List<Entity> nearTarget = entity.world.getEntitiesWithinAABBExcludingEntity(entity.getAttackTarget(), entity.getAttackTarget().getEntityBoundingBox().grow(2.0D));
					if(nearby.isEmpty() || nearby.size()==1 && nearby.get(0) ==entity.getAttackTarget() || entity.world.rand.nextInt(3)<=1)
					{
						if(nearTarget.isEmpty())
							for(int x = -1;x<=1;x++)
								for(int z=-1;z<=1;z++)
								{
									if(x==0 || z==0)
									{
										EntityEvokerFangs fang = new EntityEvokerFangs(entity.world, target.posX+x+target.motionX, target.posY, target.posZ+z+target.motionZ, 0, 5, entity);
										entity.world.spawnEntity(fang);
									}
								}
						else
						{
	                        EntityMobBullet entityBullet = new EntityMobBullet(entity.world, entity, entity.getAttackTarget(), entity.getHorizontalFacing().getAxis());
	                        entity.world.spawnEntity(entityBullet);
						}
					}
					else
					{
						for(int i = 0; i < nearby.size();i++)
						{
							Entity entityRand = nearby.get(entity.world.rand.nextInt(nearby.size()));
							if(entityRand instanceof EntityMob && entityRand!=entity.getAttackTarget())
							{
								EntityMob mob = (EntityMob) entityRand;
								mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(potionEffects[mob.world.rand.nextInt(6)]), 3600, 1));
								entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.NEUTRAL, 2F, 1.0F);
								return;
							}
						}
					}
		        }
			}
			@Override
			public int cooldown() {return 90;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}});
		
		clssMap.put(ItemBow.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
    			AIUseHelper.attackWithArrows(new EntityTippedArrow(entity.world, entity), entity, target, ItemBow.getArrowVelocity(entity.getItemInUseMaxCount()));
			}
			@Override
			public int cooldown() {return 30;}
			@Override
			public ItemType type() {return ItemType.STRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.BOTH;}
			@Override
			public boolean useHand() {return true;}});
		
		clssMap.put(ItemShield.class, new ItemAI() {
			@Override
			public void attack(EntityLiving entity, EntityLivingBase target, EnumHand hand) {
			}
			@Override
			public int cooldown() {return 50;}
			@Override
			public ItemType type() {return ItemType.NONSTRAFINGITEM;}
			@Override
			public Hand prefHand() {return Hand.OFF;}
			@Override
			public boolean useHand() {return true;}
			@Override
			public int maxUseCount() {return 200;}});
	}
}
