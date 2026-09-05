package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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
		serverbound(SetCannonLimiterItemPacket.class, SetCannonLimiterItemPacket::encode,
			SetCannonLimiterItemPacket::decode, SetCannonLimiterItemPacket::handle);
		serverbound(MeasureSpyglassDistancePacket.class, (packet, buf) -> {},
			buf -> MeasureSpyglassDistancePacket.INSTANCE, MeasureSpyglassDistancePacket::handle);
		serverbound(ControllerHoldInteractionPacket.class, ControllerHoldInteractionPacket::encode,
			ControllerHoldInteractionPacket::decode, ControllerHoldInteractionPacket::handle);
		clientbound(OpenControllerConfigPacket.class, OpenControllerConfigPacket::encode,
			OpenControllerConfigPacket::decode, OpenControllerConfigPacket::handle);
		serverbound(SetControllerConfigPacket.class, SetControllerConfigPacket::encode,
			SetControllerConfigPacket::decode, SetControllerConfigPacket::handle);
		clientbound(OpenReadyAmmoRackPacket.class, OpenReadyAmmoRackPacket::encode,
			OpenReadyAmmoRackPacket::decode, OpenReadyAmmoRackPacket::handle);
		serverbound(SwapReadyAmmoSlotsPacket.class, SwapReadyAmmoSlotsPacket::encode,
			SwapReadyAmmoSlotsPacket::decode, SwapReadyAmmoSlotsPacket::handle);
		clientbound(OpenControllerAmmunitionPacket.class, OpenControllerAmmunitionPacket::encode,
			OpenControllerAmmunitionPacket::decode, OpenControllerAmmunitionPacket::handle);
		serverbound(SelectControllerAmmunitionPacket.class, SelectControllerAmmunitionPacket::encode,
			SelectControllerAmmunitionPacket::decode, SelectControllerAmmunitionPacket::handle);
		clientbound(OpenCarouselRackPacket.class, OpenCarouselRackPacket::encode,
			OpenCarouselRackPacket::decode, OpenCarouselRackPacket::handle);
		serverbound(SwapCarouselRackSlotsPacket.class, SwapCarouselRackSlotsPacket::encode,
			SwapCarouselRackSlotsPacket::decode, SwapCarouselRackSlotsPacket::handle);
	}

	private static <T> void serverbound(Class<T> type,
		java.util.function.BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
		java.util.function.Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
		java.util.function.BiConsumer<T, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler) {
		CHANNEL.messageBuilder(type, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(encoder).decoder(decoder).consumerMainThread((packet, context) -> {
				handler.accept(packet, context);
				context.get().setPacketHandled(true);
			}).add();
	}

	private static <T> void clientbound(Class<T> type,
		java.util.function.BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
		java.util.function.Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
		java.util.function.BiConsumer<T, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler) {
		CHANNEL.messageBuilder(type, packetId++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(encoder).decoder(decoder).consumerMainThread((packet, context) -> {
				handler.accept(packet, context);
				context.get().setPacketHandled(true);
			}).add();
	}

	public static void sendToServer(SetCannonLimiterItemPacket packet) {
		CHANNEL.sendToServer(packet);
	}

	public static void sendToServer(MeasureSpyglassDistancePacket packet) { CHANNEL.sendToServer(packet); }
	public static void sendToServer(ControllerHoldInteractionPacket packet) { CHANNEL.sendToServer(packet); }
	public static void sendToServer(SetControllerConfigPacket packet) { CHANNEL.sendToServer(packet); }
	public static void sendToServer(SwapReadyAmmoSlotsPacket packet) { CHANNEL.sendToServer(packet); }
	public static void sendToServer(SelectControllerAmmunitionPacket packet) { CHANNEL.sendToServer(packet); }
	public static void sendToServer(SwapCarouselRackSlotsPacket packet) { CHANNEL.sendToServer(packet); }

	private static void sendToPlayer(Player player, Object packet) {
		if (player instanceof ServerPlayer serverPlayer)
			CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
	}

	public static void sendToPlayer(Player player, OpenControllerConfigPacket packet) { sendToPlayer(player, (Object) packet); }
	public static void sendToPlayer(Player player, OpenReadyAmmoRackPacket packet) { sendToPlayer(player, (Object) packet); }
	public static void sendToPlayer(Player player, OpenControllerAmmunitionPacket packet) { sendToPlayer(player, (Object) packet); }
	public static void sendToPlayer(Player player, OpenCarouselRackPacket packet) { sendToPlayer(player, (Object) packet); }
}
