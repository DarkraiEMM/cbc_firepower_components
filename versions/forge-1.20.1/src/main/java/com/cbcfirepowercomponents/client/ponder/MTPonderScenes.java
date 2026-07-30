package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTItems;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class MTPonderScenes {
	private MTPonderScenes() {
	}

	public static void register(PonderSceneRegistrationHelper<ResourceLocation> registration) {
		PonderSceneRegistrationHelper<Item> helper = registration.withKeyFunction(BuiltInRegistries.ITEM::getKey);

		helper.forComponents(MTItems.COMPACT_CANNON_MOUNT.get(), MTItems.COMPACT_AUTOCANNON_MOUNT.get())
			.addStoryBoard("mounts/compact", CannonMountScenes::compact);
		helper.forComponents(MTItems.AUTOCANNON_AMMO_FEED.get(), MTItems.LARGE_AUTOCANNON_AMMO_BOX.get())
			.addStoryBoard("ammo/autocannon_feed", AmmunitionScenes::autocannonFeed);
		helper.forComponents(MTItems.CANNON_MAGAZINE_LOADER.get())
			.addStoryBoard("ammo/magazine_loader", AmmunitionScenes::magazineLoader);
		helper.forComponents(MTItems.CANNON_LIMITER.get())
			.addStoryBoard("equipment/limiter", EquipmentScenes::limiter);
		helper.forComponents(MTItems.SLEEVE_MACHINE_GUN_SHIELD.get())
			.addStoryBoard("equipment/shield", EquipmentScenes::shield);
	}
}
