package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.client.ponder.MTPonderPlugin;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTEntityTypes;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonProjectileRenderer;

@Mod.EventBusSubscriber(modid = FirepowerComponents.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MTClientEvents {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> PonderIndex.addPlugin(new MTPonderPlugin()));
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(MTKeyMappings.MEASURE_DISTANCE);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), CompactCannonMountLimiterRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CAROUSEL_AMMUNITION_RACK.get(), CarouselAmmunitionRackRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.LARGE_AUTOCANNON.get(), LargeAutocannonBlockRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.LARGE_AUTOCANNON_BREECH.get(), LargeAutocannonBreechRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_AP_PROJECTILE.get(), AutocannonProjectileRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_HE_PROJECTILE.get(), AutocannonProjectileRenderer::new);
	}

}
