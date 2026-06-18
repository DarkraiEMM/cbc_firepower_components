package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MTNetwork {
	private static final String PROTOCOL = "1";
	private static int packetId;
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(FirepowerComponents.MOD_ID, "main"),
		() -> PROTOCOL,
		PROTOCOL::equals,
		PROTOCOL::equals);

	public static void register() {
		CHANNEL.messageBuilder(SetCannonLimiterItemPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(SetCannonLimiterItemPacket::encode)
			.decoder(SetCannonLimiterItemPacket::decode)
			.consumerMainThread(SetCannonLimiterItemPacket::handle)
			.add();
	}

	public static void sendToServer(SetCannonLimiterItemPacket packet) {
		CHANNEL.sendToServer(packet);
	}
}
