package com.flemmli97.improvedmobs.asm;

import com.flemmli97.improvedmobs.entity.ai.NewWalkNodeProcessor;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ASMMethods {
	
	public static PathFinder pathFinder(PathNavigateGround navigator) {
		NewWalkNodeProcessor walkNode =  new NewWalkNodeProcessor();
		EntityLiving entity = ObfuscationReflectionHelper.getPrivateValue(PathNavigate.class, navigator, "theEntity", "field_75515_a");
		walkNode.setBreakBlocks(entity.getTags().contains("Breaker"));
		walkNode.setCanEnterDoors(true);
		ObfuscationReflectionHelper.setPrivateValue(PathNavigate.class, navigator, walkNode, "nodeProcessor", "field_179695_a");
        return new PathFinder(walkNode);
   }
}
