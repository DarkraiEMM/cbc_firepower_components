package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FirepowerComponents.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MTClientEvents {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), CompactCannonMountLimiterRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderRenderer::new);
	}

}
