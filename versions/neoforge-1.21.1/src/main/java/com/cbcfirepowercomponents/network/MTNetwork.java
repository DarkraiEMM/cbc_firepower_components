package com.cbcfirepowercomponents.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MTNetwork {
	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(SetCannonLimiterItemPacket.TYPE, SetCannonLimiterItemPacket.STREAM_CODEC,
			SetCannonLimiterItemPacket::handle);
	}

	public static void sendToServer(SetCannonLimiterItemPacket packet) {
		PacketDistributor.sendToServer(packet);
	}
}
