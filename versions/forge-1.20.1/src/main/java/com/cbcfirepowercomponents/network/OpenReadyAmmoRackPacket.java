package com.cbcfirepowercomponents.network;

import java.util.ArrayList;
import java.util.List;

import com.cbcfirepowercomponents.client.MTClientPayloadHandler;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenReadyAmmoRackPacket(BlockPos pos,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) {

	public static OpenReadyAmmoRackPacket from(ReadyAmmunitionCompartmentBlockEntity compartment) {
		return new OpenReadyAmmoRackPacket(compartment.getBlockPos(), compartment.getRoundSlotsSnapshot());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.slots.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundPair pair : this.slots) {
			buf.writeItem(pair.projectile());
			buf.writeItem(pair.propellant());
		}
	}

	public static OpenReadyAmmoRackPacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int size = Math.min(buf.readVarInt(), ReadyAmmunitionCompartmentBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
				buf.readItem(), buf.readItem()));
		return new OpenReadyAmmoRackPacket(pos, slots);
	}

	public static void handle(OpenReadyAmmoRackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		MTClientPayloadHandler.openReadyAmmoRack(packet);
	}

}
