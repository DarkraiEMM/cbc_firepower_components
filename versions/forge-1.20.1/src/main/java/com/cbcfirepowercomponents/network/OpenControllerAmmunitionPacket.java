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

public record OpenControllerAmmunitionPacket(BlockPos controllerPos,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.controllerPos);
		buf.writeVarInt(this.options.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundType option : this.options) {
			buf.writeItem(option.projectile());
			buf.writeItem(option.propellant());
			buf.writeVarInt(option.count());
			buf.writeBoolean(option.selected());
		}
	}

	public static OpenControllerAmmunitionPacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int size = Math.min(buf.readVarInt(), ReadyAmmunitionCompartmentBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			options.add(new ReadyAmmunitionCompartmentBlockEntity.RoundType(
				buf.readItem(), buf.readItem(),
				buf.readVarInt(), buf.readBoolean()));
		return new OpenControllerAmmunitionPacket(pos, options);
	}

	public static void handle(OpenControllerAmmunitionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		MTClientPayloadHandler.openControllerAmmunition(packet);
	}

}
