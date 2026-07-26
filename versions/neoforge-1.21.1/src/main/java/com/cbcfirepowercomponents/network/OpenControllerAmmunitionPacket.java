package com.cbcfirepowercomponents.network;

import java.util.ArrayList;
import java.util.List;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.client.MTClientPayloadHandler;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenControllerAmmunitionPacket(BlockPos controllerPos,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options) implements CustomPacketPayload {
	public static final Type<OpenControllerAmmunitionPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "open_controller_ammunition"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenControllerAmmunitionPacket> STREAM_CODEC =
		StreamCodec.ofMember(OpenControllerAmmunitionPacket::encode, OpenControllerAmmunitionPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.controllerPos);
		buf.writeVarInt(this.options.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundType option : this.options) {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, option.projectile());
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, option.propellant());
			buf.writeVarInt(option.count());
			buf.writeBoolean(option.selected());
		}
	}

	private static OpenControllerAmmunitionPacket decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int size = Math.min(buf.readVarInt(), ReadyAmmunitionCompartmentBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			options.add(new ReadyAmmunitionCompartmentBlockEntity.RoundType(
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				buf.readVarInt(), buf.readBoolean()));
		return new OpenControllerAmmunitionPacket(pos, options);
	}

	public static void handle(OpenControllerAmmunitionPacket packet, IPayloadContext context) {
		MTClientPayloadHandler.openControllerAmmunition(packet);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
