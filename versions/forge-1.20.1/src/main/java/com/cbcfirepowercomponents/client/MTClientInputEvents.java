package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.MeasureSpyglassDistancePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, value = Dist.CLIENT)
public final class MTClientInputEvents {
	private MTClientInputEvents() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase != net.minecraftforge.event.TickEvent.Phase.END)
			return;
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
