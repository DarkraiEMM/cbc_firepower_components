package com.cbcfirepowercomponents.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MTNetwork {
	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(SetCannonLimiterItemPacket.TYPE, SetCannonLimiterItemPacket.STREAM_CODEC,
			SetCannonLimiterItemPacket::handle);
		registrar.playToServer(MeasureSpyglassDistancePacket.TYPE, MeasureSpyglassDistancePacket.STREAM_CODEC,
			MeasureSpyglassDistancePacket::handle);
		registrar.playToServer(ControllerHoldInteractionPacket.TYPE, ControllerHoldInteractionPacket.STREAM_CODEC,
			ControllerHoldInteractionPacket::handle);
		registrar.playToClient(OpenControllerConfigPacket.TYPE, OpenControllerConfigPacket.STREAM_CODEC,
			OpenControllerConfigPacket::handle);
		registrar.playToServer(SetControllerConfigPacket.TYPE, SetControllerConfigPacket.STREAM_CODEC,
			SetControllerConfigPacket::handle);
		registrar.playToClient(OpenReadyAmmoRackPacket.TYPE, OpenReadyAmmoRackPacket.STREAM_CODEC,
			OpenReadyAmmoRackPacket::handle);
		registrar.playToServer(SwapReadyAmmoSlotsPacket.TYPE, SwapReadyAmmoSlotsPacket.STREAM_CODEC,
			SwapReadyAmmoSlotsPacket::handle);
		registrar.playToClient(OpenControllerAmmunitionPacket.TYPE, OpenControllerAmmunitionPacket.STREAM_CODEC,
			OpenControllerAmmunitionPacket::handle);
		registrar.playToServer(SelectControllerAmmunitionPacket.TYPE, SelectControllerAmmunitionPacket.STREAM_CODEC,
			SelectControllerAmmunitionPacket::handle);
		registrar.playToClient(OpenCarouselRackPacket.TYPE, OpenCarouselRackPacket.STREAM_CODEC,
			OpenCarouselRackPacket::handle);
		registrar.playToServer(SwapCarouselRackSlotsPacket.TYPE, SwapCarouselRackSlotsPacket.STREAM_CODEC,
			SwapCarouselRackSlotsPacket::handle);
	}

	public static void sendToServer(SetCannonLimiterItemPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(MeasureSpyglassDistancePacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(ControllerHoldInteractionPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(SetControllerConfigPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(SwapReadyAmmoSlotsPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(SelectControllerAmmunitionPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToServer(SwapCarouselRackSlotsPacket packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void sendToPlayer(Player player, OpenControllerConfigPacket packet) {
		if (player instanceof ServerPlayer serverPlayer)
			PacketDistributor.sendToPlayer(serverPlayer, packet);
	}

	public static void sendToPlayer(Player player, OpenReadyAmmoRackPacket packet) {
		if (player instanceof ServerPlayer serverPlayer)
			PacketDistributor.sendToPlayer(serverPlayer, packet);
	}

	public static void sendToPlayer(Player player, OpenControllerAmmunitionPacket packet) {
		if (player instanceof ServerPlayer serverPlayer)
			PacketDistributor.sendToPlayer(serverPlayer, packet);
	}

	public static void sendToPlayer(Player player, OpenCarouselRackPacket packet) {
		if (player instanceof ServerPlayer serverPlayer)
			PacketDistributor.sendToPlayer(serverPlayer, packet);
	}
}
