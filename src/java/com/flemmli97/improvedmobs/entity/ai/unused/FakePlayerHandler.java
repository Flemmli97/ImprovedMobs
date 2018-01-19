package com.flemmli97.improvedmobs.entity.ai.unused;

import java.lang.ref.WeakReference;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class FakePlayerHandler {
	
	private static WeakReference<FakePlayer> player;

	public static FakePlayer getFakePlayer(World world, EntityLivingBase living)
	{
		UUID uuid = UUID.randomUUID(); 
		player = new WeakReference<FakePlayer>(FakePlayerFactory.get((WorldServer) world, new GameProfile(uuid, living.getName())));
		player.get().onGround = true;
		player.get().interactionManager.setGameType(GameType.CREATIVE);
		player.get().connection = new NetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.SERVERBOUND), player.get());
		FakePlayer fakePlayer= player.get();
		return fakePlayer;
	}

	public static FakePlayer setProperties(FakePlayer player, EntityLivingBase living)
	{
		player.setPositionAndRotation(living.posX, living.posY, living.posZ, living.rotationYaw, living.rotationPitch);
		player.setHeldItem(EnumHand.MAIN_HAND, living.getHeldItemMainhand());
		player.setHeldItem(EnumHand.OFF_HAND, living.getHeldItemOffhand());
		return player;
	}
}
