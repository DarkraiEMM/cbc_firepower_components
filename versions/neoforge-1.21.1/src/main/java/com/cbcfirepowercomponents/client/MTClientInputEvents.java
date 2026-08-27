package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.MeasureSpyglassDistancePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, value = Dist.CLIENT)
public final class MTClientInputEvents {
	private MTClientInputEvents() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		while (MTKeyMappings.MEASURE_DISTANCE.consumeClick()) {
			if (minecraft.player != null
				&& minecraft.player.isUsingItem()
				&& minecraft.player.getUseItem().is(Items.SPYGLASS)) {
				MTNetwork.sendToServer(MeasureSpyglassDistancePacket.INSTANCE);
			}
		}
	}
}
