package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SwapReadyAmmoSlotsPacket(BlockPos pos, int first, int second) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.first);
		buf.writeVarInt(this.second);
	}

	public static SwapReadyAmmoSlotsPacket decode(FriendlyByteBuf buf) {
		return new SwapReadyAmmoSlotsPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SwapReadyAmmoSlotsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayer player = contextSupplier.get().getSender();
		if (player == null || player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof ReadyAmmunitionCompartmentBlockEntity compartment))
			return;
		compartment.swapRoundSlots(packet.first, packet.second);
	}

}
