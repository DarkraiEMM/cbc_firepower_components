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

public record ControllerHoldInteractionPacket(BlockPos pos, boolean openScreen) implements CustomPacketPayload {
	public static final Type<ControllerHoldInteractionPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "controller_hold_interaction"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ControllerHoldInteractionPacket> STREAM_CODEC =
		StreamCodec.ofMember(ControllerHoldInteractionPacket::encode, ControllerHoldInteractionPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeBoolean(this.openScreen);
	}

	private static ControllerHoldInteractionPacket decode(RegistryFriendlyByteBuf buf) {
		return new ControllerHoldInteractionPacket(buf.readBlockPos(), buf.readBoolean());
	}

	public static void handle(ControllerHoldInteractionPacket packet, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
			|| player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		if (packet.openScreen)
			controller.openConfiguration(player);
		else
			controller.handlePrimaryInteraction(player);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
