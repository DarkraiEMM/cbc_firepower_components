package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.network.OpenControllerConfigPacket;
import com.cbcfirepowercomponents.network.OpenControllerAmmunitionPacket;
import com.cbcfirepowercomponents.network.OpenReadyAmmoRackPacket;
import com.cbcfirepowercomponents.network.OpenCarouselRackPacket;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MTClientPayloadHandler {
	private MTClientPayloadHandler() {}

	public static void openControllerConfig(OpenControllerConfigPacket packet) {
		Minecraft.getInstance().setScreen(new AutomaticCannonControllerScreen(
			packet.pos(), packet.fireMode(), packet.coordinationMode(), packet.signalStrength()));
	}

	public static void openReadyAmmoRack(OpenReadyAmmoRackPacket packet) {
		Minecraft.getInstance().setScreen(new ReadyAmmunitionRackScreen(packet.pos(), packet.slots()));
	}

	public static void openControllerAmmunition(OpenControllerAmmunitionPacket packet) {
		Minecraft.getInstance().setScreen(new ControllerAmmunitionSelectionScreen(
			packet.controllerPos(), packet.options()));
	}

	public static void openCarouselRack(OpenCarouselRackPacket packet) {
		Minecraft.getInstance().setScreen(new CarouselAmmunitionRackScreen(
			packet.pos(), packet.currentIndex(), packet.targetIndex(), packet.slots()));
	}
}
