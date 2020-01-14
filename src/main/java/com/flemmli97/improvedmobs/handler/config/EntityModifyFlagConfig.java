package com.flemmli97.improvedmobs.handler.config;

import java.util.EnumSet;
import java.util.Map;

import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.flemmli97.tenshilib.common.javahelper.ArrayUtils;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EntityModifyFlagConfig implements IConfigArrayValue<EntityModifyFlagConfig> {

	private Map<String, EnumSet<Flags>> map = Maps.newHashMap();

	public EntityModifyFlagConfig() {
		//Init default values as blacklist
		for(EntityEntry entry : ForgeRegistries.ENTITIES){
			if(EntitySlime.class.isAssignableFrom(entry.getEntityClass()) || EntityGhast.class.isAssignableFrom(entry.getEntityClass()))
				continue;
			if(!EntityLiving.class.isAssignableFrom(entry.getEntityClass()))
				continue;
			if(EntityMob.class.isAssignableFrom(entry.getEntityClass()))
				if(!IEntityOwnable.class.isAssignableFrom(entry.getEntityClass()))
					continue;
			ResourceLocation res = entry.getRegistryName();
			map.put(res.toString(), EnumSet.of(Flags.ALL));
		}
	}

	public boolean testForFlag(EntityLiving living, Flags flag, boolean reverse) {
		ResourceLocation res = EntityList.getKey(living);
		if(res == null)
			return false;

		EnumSet<Flags> set = this.map.get(res.toString());
		if(set == null)
			set = this.map.get(res.getResourceDomain());

		if(set != null)
			return reverse ^ set.contains(Flags.REVERSE) ^ (set.contains(Flags.ALL) || set.contains(flag));
		return reverse;
	}

	@Override
	public EntityModifyFlagConfig readFromString(String[] s) {
		map.clear();
		for(String val : s){
			String[] subs = val.split("\\|");

			EnumSet<Flags> set = null;
			if(subs.length == 1)
				set = EnumSet.of(Flags.ALL);
			else{
				set = EnumSet.noneOf(Flags.class);
				for(int i = 1; i < subs.length; i++)
					set.add(Flags.valueOf(subs[i].trim()));
			}
			map.put(subs[0].trim(), set);
		}
		return this;
	}

	@Override
	public String[] writeToString() {
		String[] s = new String[map.size()];
		int id = 0;
		for(String key : map.keySet()){
			String val = key;
			for(Flags f : map.get(key)){
				if(f != Flags.ALL)
					val += "|" + f.name();
			}
			s[id] = val;
			id++;
		}
		return s;
	}

	@Override
	public String usage() {
		String[] str = new String[] {"<entity registry name> followed by any of:", "[" + ArrayUtils.arrayToString(Flags.values()) + "].", "Leave empty to apply them all and REVERSE to reverse all flags. Some flags do nothing for certain mobs!",
				"example: minecraft:sheep|REVERSE|ATTRIBUTES will add sheep to attributes modification (since default is a blacklist)", "or minecraft:sheep|ATTRIBUTES will add sheep to everything except attributes"};
		return String.join("\n", str) + "\n";
	}

	public static enum Flags {

		ALL,
		ATTRIBUTES,
		ARMOR,
		HELDITEMS,
		BLOCKBREAK,
		USEITEM,
		LADDER,
		STEAL,
		SWIMMRIDE,
		TARGETVILLAGER,
		//TARGETPLAYER,
		//TARGETHURT,
		REVERSE;

	}
}
