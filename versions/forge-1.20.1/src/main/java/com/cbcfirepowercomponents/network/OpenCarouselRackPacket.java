package com.cbcfirepowercomponents.network;

import java.util.ArrayList;
import java.util.List;

import com.cbcfirepowercomponents.client.MTClientPayloadHandler;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenCarouselRackPacket(BlockPos pos, int currentIndex, int targetIndex,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) {

	public static OpenCarouselRackPacket from(CarouselAmmunitionRackBlockEntity rack) {
		return new OpenCarouselRackPacket(rack.getBlockPos(), rack.getCurrentIndex(), rack.getTargetIndex(),
			rack.getSlotsSnapshot());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.currentIndex);
		buf.writeVarInt(this.targetIndex + 1);
		buf.writeVarInt(this.slots.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundPair pair : this.slots) {
			buf.writeItem(pair.projectile());
			buf.writeItem(pair.propellant());
		}
	}

	public static OpenCarouselRackPacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int current = buf.readVarInt();
		int target = buf.readVarInt() - 1;
		int size = Math.min(buf.readVarInt(), CarouselAmmunitionRackBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
				buf.readItem(), buf.readItem()));
		return new OpenCarouselRackPacket(pos, current, target, slots);
	}

	public static void handle(OpenCarouselRackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		MTClientPayloadHandler.openCarouselRack(packet);
	}

}
