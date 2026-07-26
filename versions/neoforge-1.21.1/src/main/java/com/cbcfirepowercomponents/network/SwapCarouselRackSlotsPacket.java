package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwapCarouselRackSlotsPacket(BlockPos pos, int first, int second) implements CustomPacketPayload {
	public static final Type<SwapCarouselRackSlotsPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "swap_carousel_rack_slots"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SwapCarouselRackSlotsPacket> STREAM_CODEC =
		StreamCodec.ofMember(SwapCarouselRackSlotsPacket::encode, SwapCarouselRackSlotsPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.first);
		buf.writeVarInt(this.second);
	}

	private static SwapCarouselRackSlotsPacket decode(RegistryFriendlyByteBuf buf) {
		return new SwapCarouselRackSlotsPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SwapCarouselRackSlotsPacket packet, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
			|| player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 100.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof CarouselAmmunitionRackBlockEntity rack))
			return;
		rack.swapSlots(packet.first, packet.second);
	}

	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
