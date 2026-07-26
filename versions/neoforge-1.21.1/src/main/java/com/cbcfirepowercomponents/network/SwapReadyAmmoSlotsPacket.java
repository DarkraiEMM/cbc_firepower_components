package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwapReadyAmmoSlotsPacket(BlockPos pos, int first, int second) implements CustomPacketPayload {
	public static final Type<SwapReadyAmmoSlotsPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "swap_ready_ammo_slots"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SwapReadyAmmoSlotsPacket> STREAM_CODEC =
		StreamCodec.ofMember(SwapReadyAmmoSlotsPacket::encode, SwapReadyAmmoSlotsPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.first);
		buf.writeVarInt(this.second);
	}

	private static SwapReadyAmmoSlotsPacket decode(RegistryFriendlyByteBuf buf) {
		return new SwapReadyAmmoSlotsPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SwapReadyAmmoSlotsPacket packet, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
			|| player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof ReadyAmmunitionCompartmentBlockEntity compartment))
			return;
		compartment.swapRoundSlots(packet.first, packet.second);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
