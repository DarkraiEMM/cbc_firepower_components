package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.client.MTClientPayloadHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenControllerConfigPacket(BlockPos pos, int fireMode, int coordinationMode, int signalStrength) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.fireMode);
		buf.writeVarInt(this.coordinationMode);
		buf.writeVarInt(this.signalStrength);
	}

	public static OpenControllerConfigPacket decode(FriendlyByteBuf buf) {
		return new OpenControllerConfigPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(OpenControllerConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		MTClientPayloadHandler.openControllerConfig(packet);
	}

}
