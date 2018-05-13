package com.flemmli97.improvedmobs.asm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Maps;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer{

	private static Map<String, Transform> patches = Maps.newHashMap();
	private static Map<String, Method> classMethod = Maps.newHashMap();
	private static final Logger logger = LogManager.getLogger("ImprovedMobs/ASM");

	static
	{
		patches.put("net.minecraft.pathfinding.PathNavigateGround", pathNavigateGround());
		classMethod.put("net.minecraft.pathfinding.PathNavigateGround", 
				new Method("getPathFinder","func_179679_a","a", "()Lnet/minecraft/pathfinding/PathFinder;", "()Lazj;"));
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(patches.containsKey(transformedName))
		{
			return transform(basicClass, classMethod.get(transformedName), patches.get(transformedName));
		}
		return basicClass;
	}
		
	private static byte[] transform(byte[] clss, Method m, Transform transform)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(clss);
		classReader.accept(classNode, 0);
		for(MethodNode method : classNode.methods)
		{
			if( (method.name.equals(m.name)||method.name.equals(m.srgName)||method.name.equals(m.obfName) ) && (method.desc.equals(m.desc)||method.desc.equals(m.obfDesc)))
			{
				transform.apply(classNode, method);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				return writer.toByteArray();
			}
		}
		return clss;
	}
	
	private static Transform pathNavigateGround()
	{
		Transform t = new Transform() {
			@Override
			public void apply(ClassNode clss, MethodNode method) {
					debug("Patching PathNavigateGround");
					Iterator<AbstractInsnNode> it = method.instructions.iterator();
					List<AbstractInsnNode> list = new LinkedList<AbstractInsnNode>();
					while(it.hasNext())
					{
						list.add(it.next());
					}
					for(AbstractInsnNode node : list)
						method.instructions.remove(node);
					InsnList inject = new InsnList();
					inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/improvedmobs/asm/ASMMethods", "pathFinder", 
					"(Lnet/minecraft/pathfinding/PathNavigateGround;)Lnet/minecraft/pathfinding/PathFinder;", false));
					inject.add(new InsnNode(Opcodes.ARETURN));
					method.instructions.insert(inject);
			}};
		return t;
	}
	
	private interface Transform
	{
		public void apply(ClassNode clss, MethodNode method);
	}
	
	static void debug(String debug)
	{
		logger.debug("[ImprovedMobs/ASM]: " + debug);
	}

	private static class Method
	{
		final String name, srgName, obfName, desc, obfDesc;
		
		Method(String name, String srgName, String obfName, String desc, String obfDesc)
		{
			this.name = name;
			this.srgName=srgName;
			this.obfName=obfName;
			this.desc=desc;
			this.obfDesc=obfDesc;
		}
	}
}
