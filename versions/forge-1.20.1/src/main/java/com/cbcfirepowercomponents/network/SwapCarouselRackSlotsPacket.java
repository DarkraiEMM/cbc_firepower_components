package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SwapCarouselRackSlotsPacket(BlockPos pos, int first, int second) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.first);
		buf.writeVarInt(this.second);
	}

	public static SwapCarouselRackSlotsPacket decode(FriendlyByteBuf buf) {
		return new SwapCarouselRackSlotsPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SwapCarouselRackSlotsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayer player = contextSupplier.get().getSender();
		if (player == null || player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 100.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof CarouselAmmunitionRackBlockEntity rack))
			return;
		rack.swapSlots(packet.first, packet.second);
	}

}
