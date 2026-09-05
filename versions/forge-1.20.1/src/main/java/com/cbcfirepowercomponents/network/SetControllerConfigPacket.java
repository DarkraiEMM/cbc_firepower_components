package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetControllerConfigPacket(BlockPos pos, int fireMode, int coordinationMode, int signalStrength) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.fireMode);
		buf.writeVarInt(this.coordinationMode);
		buf.writeVarInt(this.signalStrength);
	}

	public static SetControllerConfigPacket decode(FriendlyByteBuf buf) {
		return new SetControllerConfigPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SetControllerConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayer player = contextSupplier.get().getSender();
		if (player == null || player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		controller.setConfiguration(packet.fireMode, packet.coordinationMode, packet.signalStrength);
		player.displayClientMessage(controller.getStatusMessage(), true);
	}

}
