package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ControllerHoldInteractionPacket(BlockPos pos, boolean openScreen) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeBoolean(this.openScreen);
	}

	public static ControllerHoldInteractionPacket decode(FriendlyByteBuf buf) {
		return new ControllerHoldInteractionPacket(buf.readBlockPos(), buf.readBoolean());
	}

	public static void handle(ControllerHoldInteractionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayer player = contextSupplier.get().getSender();
		if (player == null || player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0
			|| !(player.level().getBlockEntity(packet.pos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		if (packet.openScreen)
			controller.openConfiguration(player);
		else
			controller.handlePrimaryInteraction(player);
	}

}
