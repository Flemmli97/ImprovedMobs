package com.flemmli97.improvedmobs.handler.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class MobClassMapConfig implements IConfigArrayValue<MobClassMapConfig> {

	private Map<ResourceLocation, List<Class<? extends EntityLiving>>> map = Maps.newLinkedHashMap();
	public Map<ResourceLocation, Predicate<Class<? extends EntityLiving>>> preds = Maps.newHashMap();
	public MobClassMapConfig(String[] s) {
		this.readFromString(s);
	}

	@Nullable
	public List<Class<? extends EntityLiving>> get(ResourceLocation res) {
		return map.get(res);
	}

	@Override
	public MobClassMapConfig readFromString(String[] ss) {
		for(String s : ss){
			String[] sub = s.split("-");
			Class<? extends EntityLiving> clss = null;
			try{
				clss = this.findClass(sub[1]);
			}catch(ClassNotFoundException e1){
				try{
					clss = this.findClass("net.minecraft.entity.monster." + sub[1]);
				}catch(ClassNotFoundException e2){
					try{
						clss = this.findClass("net.minecraft.entity.passive." + sub[1]);
					}catch(ClassNotFoundException e3){
						ImprovedMobs.logger.error("No Class for {}!", sub[1]);
					}
				}
			}
			if(clss != null)
				map.put(new ResourceLocation(sub[0]), this.add(map.getOrDefault(new ResourceLocation(sub[0]), Lists.newArrayList()), clss));
		}
		return this;
	}

	private List<Class<? extends EntityLiving>> add(List<Class<? extends EntityLiving>> list, Class<? extends EntityLiving> val) {
		list.add(val);
		return list;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Class<? extends EntityLiving> findClass(String name) throws ClassNotFoundException {
		if(name.contains(":")){
			Class<? extends Entity> clss = EntityList.getClass(new ResourceLocation(name));
			if(clss != null && EntityLiving.class.isAssignableFrom(clss))
				return (Class<? extends EntityLiving>) clss;
		}
		Class<?> clss = Class.forName(name);
		if(clss != null && EntityLiving.class.isAssignableFrom(clss))
			return (Class<? extends EntityLiving>) clss;
		return null;
	}

	@Override
	public String[] writeToString() {
		List<String> l = Lists.newArrayList();
		for(Entry<ResourceLocation, List<Class<? extends EntityLiving>>> ent : map.entrySet()){
			for(Class<? extends EntityLiving> clss : ent.getValue()){
				l.add(ent.getKey().toString() + "-" + clss.getName());
			}
		}
		return l.toArray(new String[0]);
	}

	@Override
	public String usage() {
		return "[mob id]-[mob id or class name] where second value is the target.\n e.g. minecraft:zombie - minecraft:skeleton makes all zombies target skeletons";
	}

}
