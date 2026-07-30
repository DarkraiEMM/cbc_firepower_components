package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.rangefinder.SpyglassRangefinder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MeasureSpyglassDistancePacket() implements CustomPacketPayload {
	public static final MeasureSpyglassDistancePacket INSTANCE = new MeasureSpyglassDistancePacket();
	public static final Type<MeasureSpyglassDistancePacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "measure_spyglass_distance"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MeasureSpyglassDistancePacket> STREAM_CODEC =
		StreamCodec.unit(INSTANCE);

	public static void handle(MeasureSpyglassDistancePacket packet, IPayloadContext context) {
		Player contextPlayer = context.player();
		if (contextPlayer instanceof ServerPlayer player)
			SpyglassRangefinder.measure(player);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
