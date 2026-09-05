package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.rangefinder.SpyglassRangefinder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MeasureSpyglassDistancePacket() {
	public static final MeasureSpyglassDistancePacket INSTANCE = new MeasureSpyglassDistancePacket();

	public static void handle(MeasureSpyglassDistancePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		Player contextPlayer = contextSupplier.get().getSender();
		if (contextPlayer instanceof ServerPlayer player)
			SpyglassRangefinder.measure(player);
	}

}
