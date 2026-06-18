package com.cbcfirepowercomponents.network;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterItem;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterSettings;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetCannonLimiterItemPacket(InteractionHand hand, CannonLimiterSettings settings) implements CustomPacketPayload {
	public static final Type<SetCannonLimiterItemPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "set_cannon_limiter_item"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetCannonLimiterItemPacket> STREAM_CODEC =
		StreamCodec.ofMember(SetCannonLimiterItemPacket::encode, SetCannonLimiterItemPacket::decode);

	private void encode(RegistryFriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		writeSettings(buf, this.settings);
	}

	private static SetCannonLimiterItemPacket decode(RegistryFriendlyByteBuf buf) {
		return new SetCannonLimiterItemPacket(buf.readEnum(InteractionHand.class), readSettings(buf));
	}

	public static void handle(SetCannonLimiterItemPacket packet, IPayloadContext context) {
		Player contextPlayer = context.player();
		if (!(contextPlayer instanceof ServerPlayer player))
			return;
		ItemStack stack = player.getItemInHand(packet.hand);
		if (stack.getItem() instanceof CannonLimiterItem)
			CannonLimiterSettings.save(stack, packet.settings);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private static void writeSettings(RegistryFriendlyByteBuf buf, CannonLimiterSettings settings) {
		buf.writeBoolean(settings.hasPitchMin);
		buf.writeFloat(settings.pitchMin);
		buf.writeBoolean(settings.hasPitchMax);
		buf.writeFloat(settings.pitchMax);
		buf.writeBoolean(settings.hasYawMin);
		buf.writeFloat(settings.yawMin);
		buf.writeBoolean(settings.hasYawMax);
		buf.writeFloat(settings.yawMax);
	}

	private static CannonLimiterSettings readSettings(RegistryFriendlyByteBuf buf) {
		CannonLimiterSettings settings = new CannonLimiterSettings();
		settings.hasPitchMin = buf.readBoolean();
		settings.pitchMin = buf.readFloat();
		settings.hasPitchMax = buf.readBoolean();
		settings.pitchMax = buf.readFloat();
		settings.hasYawMin = buf.readBoolean();
		settings.yawMin = buf.readFloat();
		settings.hasYawMax = buf.readBoolean();
		settings.yawMax = buf.readFloat();
		return settings;
	}
}
