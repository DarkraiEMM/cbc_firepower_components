package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MTClientEvents {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), CompactCannonMountLimiterRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderRenderer::new);
	}
}
