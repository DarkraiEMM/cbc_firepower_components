package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.client.MTClientPayloadHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenControllerConfigPacket(BlockPos pos, int fireMode, int coordinationMode, int signalStrength)
	implements CustomPacketPayload {
	public static final Type<OpenControllerConfigPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "open_controller_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenControllerConfigPacket> STREAM_CODEC =
		StreamCodec.ofMember(OpenControllerConfigPacket::encode, OpenControllerConfigPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.fireMode);
		buf.writeVarInt(this.coordinationMode);
		buf.writeVarInt(this.signalStrength);
	}

	private static OpenControllerConfigPacket decode(RegistryFriendlyByteBuf buf) {
		return new OpenControllerConfigPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(OpenControllerConfigPacket packet, IPayloadContext context) {
		MTClientPayloadHandler.openControllerConfig(packet);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
