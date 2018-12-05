package com.flemmli97.improvedmobs.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
/**
 * Using reflection to replace pathnavigate breaks ai... (like EntityAIAvoidEntity)
 *
 */
@MCVersion(value="1.10.2")
@Name(value="ImprovedMobs/ASM")
public class ASMLoader implements IFMLLoadingPlugin{

	public static boolean deObfEnviroment = false;
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ASMTransformer.class.getName()};	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		deObfEnviroment = !(Boolean)data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
