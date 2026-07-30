package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetControllerConfigPacket(BlockPos pos, int fireMode, int coordinationMode, int signalStrength)
	implements CustomPacketPayload {
	public static final Type<SetControllerConfigPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "set_controller_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetControllerConfigPacket> STREAM_CODEC =
		StreamCodec.ofMember(SetControllerConfigPacket::encode, SetControllerConfigPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.fireMode);
		buf.writeVarInt(this.coordinationMode);
		buf.writeVarInt(this.signalStrength);
	}

	private static SetControllerConfigPacket decode(RegistryFriendlyByteBuf buf) {
		return new SetControllerConfigPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(SetControllerConfigPacket packet, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
			|| player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		controller.setConfiguration(packet.fireMode, packet.coordinationMode, packet.signalStrength);
		player.displayClientMessage(controller.getStatusMessage(), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
