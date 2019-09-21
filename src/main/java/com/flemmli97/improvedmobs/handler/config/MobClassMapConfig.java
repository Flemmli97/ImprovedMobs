package com.flemmli97.improvedmobs.handler.config;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class MobClassMapConfig implements IConfigArrayValue<MobClassMapConfig>{

	private Map<ResourceLocation, Class<? extends EntityLiving>> map = Maps.newLinkedHashMap();
	
	public MobClassMapConfig(String[] s)
	{
		this.readFromString(s);
	}
	
	@Nullable
	public Class<? extends EntityLiving> get(ResourceLocation res)
	{
		return map.get(res);
	}
	
	@Override
	public MobClassMapConfig readFromString(String[] ss) 
	{
		for(String s : ss)
		{
			String[] sub = s.split("-");
			try
			{
				map.put(new ResourceLocation(sub[0]), this.findClass(sub[1]));
			}
			catch(ClassNotFoundException e1)
			{
				try
				{
					map.put(new ResourceLocation(sub[0]), this.findClass("net.minecraft.entity.monster."+sub[1]));

				}
				catch(ClassNotFoundException e2)
				{
					try
					{
						map.put(new ResourceLocation(sub[0]), this.findClass("net.minecraft.entity.passive."+sub[1]));

					}
					catch(ClassNotFoundException e3)
					{
						ImprovedMobs.logger.error("No Class for {}!", sub[1]);
					}
				}
			}
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private Class<? extends EntityLiving> findClass(String name) throws ClassNotFoundException
	{
		if(name.contains(":"))
		{
			Class<? extends Entity> clss = EntityList.getClass(new ResourceLocation(name));
			if(clss!=null && EntityLiving.class.isAssignableFrom(clss))
				return (Class<? extends EntityLiving>) clss;
		}
		Class<?> clss = Class.forName(name);
		if(clss!=null && EntityLiving.class.isAssignableFrom(clss))
			return (Class<? extends EntityLiving>) clss;
		return null;
	}

	@Override
	public String[] writeToString() 
	{
		String[] s = new String[map.size()];
		int i = 0;
		for(Entry<ResourceLocation, Class<? extends EntityLiving>> ent:map.entrySet())
		{
			s[i] = ent.getKey().toString()+"-"+ent.getValue().getName();
			i++;
		}
		return s;
	}

	@Override
	public String usage() 
	{
		return "[mob id e.g. minecraft:skeleton]-[mob id or class name e.g. EntityZombie]";
	}

}
