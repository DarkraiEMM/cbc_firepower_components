package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectControllerAmmunitionPacket(BlockPos controllerPos, ItemStack projectile, ItemStack propellant) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.controllerPos);
		buf.writeItem(this.projectile);
		buf.writeItem(this.propellant);
	}

	public static SelectControllerAmmunitionPacket decode(FriendlyByteBuf buf) {
		return new SelectControllerAmmunitionPacket(buf.readBlockPos(),
			buf.readItem(), buf.readItem());
	}

	public static void handle(SelectControllerAmmunitionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayer player = contextSupplier.get().getSender();
		if (player == null || player.distanceToSqr(Vec3.atCenterOf(packet.controllerPos)) > 64.0
			|| !(player.level().getBlockEntity(packet.controllerPos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		boolean changed = controller.selectAmmunition(packet.projectile, packet.propellant);
		player.displayClientMessage(Component.translatable(changed
			? "block.cbc_firepower_components.automatic_cannon_controller.ammunition_changed"
			: "block.cbc_firepower_components.automatic_cannon_controller.no_matching_ammunition"), true);
	}

}
