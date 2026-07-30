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

public record OpenReadyAmmoRackPacket(BlockPos pos,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) implements CustomPacketPayload {
	public static final Type<OpenReadyAmmoRackPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "open_ready_ammo_rack"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenReadyAmmoRackPacket> STREAM_CODEC =
		StreamCodec.ofMember(OpenReadyAmmoRackPacket::encode, OpenReadyAmmoRackPacket::decode);

	public static OpenReadyAmmoRackPacket from(ReadyAmmunitionCompartmentBlockEntity compartment) {
		return new OpenReadyAmmoRackPacket(compartment.getBlockPos(), compartment.getRoundSlotsSnapshot());
	}

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.slots.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundPair pair : this.slots) {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pair.projectile());
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pair.propellant());
		}
	}

	private static OpenReadyAmmoRackPacket decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int size = Math.min(buf.readVarInt(), ReadyAmmunitionCompartmentBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)));
		return new OpenReadyAmmoRackPacket(pos, slots);
	}

	public static void handle(OpenReadyAmmoRackPacket packet, IPayloadContext context) {
		MTClientPayloadHandler.openReadyAmmoRack(packet);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
