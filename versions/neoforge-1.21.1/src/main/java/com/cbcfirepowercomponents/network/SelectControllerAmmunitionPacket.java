package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectControllerAmmunitionPacket(BlockPos controllerPos, ItemStack projectile, ItemStack propellant)
	implements CustomPacketPayload {
	public static final Type<SelectControllerAmmunitionPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "select_controller_ammunition"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectControllerAmmunitionPacket> STREAM_CODEC =
		StreamCodec.ofMember(SelectControllerAmmunitionPacket::encode, SelectControllerAmmunitionPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.controllerPos);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.projectile);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.propellant);
	}

	private static SelectControllerAmmunitionPacket decode(RegistryFriendlyByteBuf buf) {
		return new SelectControllerAmmunitionPacket(buf.readBlockPos(),
			ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
	}

	public static void handle(SelectControllerAmmunitionPacket packet, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
			|| player.distanceToSqr(Vec3.atCenterOf(packet.controllerPos)) > 64.0
			|| !(player.level().getBlockEntity(packet.controllerPos) instanceof AutomaticCannonControllerBlockEntity controller))
			return;
		boolean changed = controller.selectAmmunition(packet.projectile, packet.propellant);
		player.displayClientMessage(Component.translatable(changed
			? "block.cbc_firepower_components.automatic_cannon_controller.ammunition_changed"
			: "block.cbc_firepower_components.automatic_cannon_controller.no_matching_ammunition"), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
