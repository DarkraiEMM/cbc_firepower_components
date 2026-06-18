package com.cbcfirepowercomponents.network;

import java.util.function.Supplier;

import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterItem;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterSettings;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SetCannonLimiterItemPacket {
	private final InteractionHand hand;
	private final CannonLimiterSettings settings;

	public SetCannonLimiterItemPacket(InteractionHand hand, CannonLimiterSettings settings) {
		this.hand = hand;
		this.settings = settings;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		writeSettings(buf, this.settings);
	}

	public static SetCannonLimiterItemPacket decode(FriendlyByteBuf buf) {
		return new SetCannonLimiterItemPacket(buf.readEnum(InteractionHand.class), readSettings(buf));
	}

	public static void handle(SetCannonLimiterItemPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		ServerPlayer player = context.getSender();
		if (player != null) {
			ItemStack stack = player.getItemInHand(packet.hand);
			if (stack.getItem() instanceof CannonLimiterItem)
				CannonLimiterSettings.save(stack, packet.settings);
		}
		context.setPacketHandled(true);
	}

	private static void writeSettings(FriendlyByteBuf buf, CannonLimiterSettings settings) {
		buf.writeBoolean(settings.hasPitchMin);
		buf.writeFloat(settings.pitchMin);
		buf.writeBoolean(settings.hasPitchMax);
		buf.writeFloat(settings.pitchMax);
		buf.writeBoolean(settings.hasYawMin);
		buf.writeFloat(settings.yawMin);
		buf.writeBoolean(settings.hasYawMax);
		buf.writeFloat(settings.yawMax);
	}

	private static CannonLimiterSettings readSettings(FriendlyByteBuf buf) {
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
