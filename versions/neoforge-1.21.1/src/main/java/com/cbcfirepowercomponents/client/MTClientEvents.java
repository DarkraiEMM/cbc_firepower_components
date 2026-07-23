package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTEntityTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonProjectileRenderer;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MTClientEvents {
	private static final LargeAutocannonCTBehaviour LARGE_AUTOCANNON_CT = new LargeAutocannonCTBehaviour();
	private static boolean connectedTextureModelsRegistered;

	@SubscribeEvent
	public static void registerConnectedTextureModels(ModelEvent.RegisterAdditional event) {
		if (connectedTextureModelsRegistered)
			return;
		connectedTextureModelsRegistered = true;

		registerConnectedTextureModel(MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get());
		registerConnectedTextureModel(MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get());
		registerConnectedTextureModel(MTBlocks.LARGE_AUTOCANNON_BREECH.get());
		registerConnectedTextureModel(MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
		registerConnectedTextureModel(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get());
		registerConnectedTextureModel(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get());
		registerConnectedTextureModel(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
	}

	private static void registerConnectedTextureModel(Block block) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels().register(BuiltInRegistries.BLOCK.getKey(block),
			model -> new CTModel(model, LARGE_AUTOCANNON_CT));
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), CompactCannonMountLimiterRenderer::new);
		event.registerBlockEntityRenderer(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_AP_PROJECTILE.get(), AutocannonProjectileRenderer::new);
		event.registerEntityRenderer(MTEntityTypes.LARGE_AUTOCANNON_HE_PROJECTILE.get(), AutocannonProjectileRenderer::new);
	}

}
