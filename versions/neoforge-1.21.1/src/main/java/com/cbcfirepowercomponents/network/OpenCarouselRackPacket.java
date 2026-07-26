package com.cbcfirepowercomponents.network;

import java.util.ArrayList;
import java.util.List;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.client.MTClientPayloadHandler;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCarouselRackPacket(BlockPos pos, int currentIndex, int targetIndex,
	List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) implements CustomPacketPayload {
	public static final Type<OpenCarouselRackPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "open_carousel_rack"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenCarouselRackPacket> STREAM_CODEC =
		StreamCodec.ofMember(OpenCarouselRackPacket::encode, OpenCarouselRackPacket::decode);

	public static OpenCarouselRackPacket from(CarouselAmmunitionRackBlockEntity rack) {
		return new OpenCarouselRackPacket(rack.getBlockPos(), rack.getCurrentIndex(), rack.getTargetIndex(),
			rack.getSlotsSnapshot());
	}

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeVarInt(this.currentIndex);
		buf.writeVarInt(this.targetIndex + 1);
		buf.writeVarInt(this.slots.size());
		for (ReadyAmmunitionCompartmentBlockEntity.RoundPair pair : this.slots) {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pair.projectile());
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pair.propellant());
		}
	}

	private static OpenCarouselRackPacket decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int current = buf.readVarInt();
		int target = buf.readVarInt() - 1;
		int size = Math.min(buf.readVarInt(), CarouselAmmunitionRackBlockEntity.CAPACITY);
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots = new ArrayList<>(size);
		for (int i = 0; i < size; ++i)
			slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)));
		return new OpenCarouselRackPacket(pos, current, target, slots);
	}

	public static void handle(OpenCarouselRackPacket packet, IPayloadContext context) {
		MTClientPayloadHandler.openCarouselRack(packet);
	}

	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
