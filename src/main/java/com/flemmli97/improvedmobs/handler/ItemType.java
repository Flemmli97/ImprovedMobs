package com.flemmli97.improvedmobs.handler;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public enum ItemType {

	/** for items like bow*/
	BOW,
	
	/**for items like lava buckets, where entity should normal pathfind*/
	NONSTRAFINGITEM,
	
	/**for items, where entity should strafe like skeletons*/
	STRAFINGITEM,
	
	/**rest of items, which entity should behave normal*/
	NOTHING;
	
	EnumHand hand;
	ItemStack item;
	
	public void setHand(EnumHand hand)
	{
		this.hand = hand;
	}
	
	public EnumHand getHand()
	{
		return this.hand;
	}
	
	public void setItem(ItemStack item)
	{
		this.item = item;
	}
	
	public Item getItem()
	{
		return this.item!=null?this.item.getItem():Item.getItemFromBlock(Blocks.AIR);
	}
	
	public ItemStack getStack()
	{
		return this.item;
	}
}
