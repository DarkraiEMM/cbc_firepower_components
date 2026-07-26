package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.client.ponder.MTPonderPlugin;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTEntityTypes;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonProjectileRenderer;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MTClientEvents {
	private static final LargeAutocannonCTBehaviour LARGE_AUTOCANNON_CT = new LargeAutocannonCTBehaviour();
	private static boolean connectedTextureModelsRegistered;

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> PonderIndex.addPlugin(new MTPonderPlugin()));
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(MTKeyMappings.MEASURE_DISTANCE);
	}

	@SubscribeEvent
	public static void registerConnectedTextureModels(ModelEvent.RegisterAdditional event) {
		if (connectedTextureModelsRegistered)
			return;
		connectedTextureModelsRegistered = true;

		registerDynamicConnectedTextureModel(MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get());
		registerDynamicConnectedTextureModel(MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get());
		registerDynamicConnectedTextureModel(MTBlocks.LARGE_AUTOCANNON_BREECH.get());
		registerDynamicConnectedTextureModel(MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
		registerDynamicConnectedTextureModel(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get());
		registerDynamicConnectedTextureModel(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get());
		registerDynamicConnectedTextureModel(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
	}

	private static void registerDynamicConnectedTextureModel(Block block) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels().register(BuiltInRegistries.BLOCK.getKey(block),
			model -> new DynamicTwinAutocannonModel(new CTModel(model, LARGE_AUTOCANNON_CT)));
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), CompactCannonMountLimiterRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.LARGE_AUTOCANNON.get(), LargeAutocannonBlockRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.LARGE_AUTOCANNON_BREECH.get(), LargeAutocannonBreechRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CAROUSEL_AMMUNITION_RACK.get(), CarouselAmmunitionRackRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_AP_PROJECTILE.get(), AutocannonProjectileRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_HE_PROJECTILE.get(), AutocannonProjectileRenderer::new);
	}

}
