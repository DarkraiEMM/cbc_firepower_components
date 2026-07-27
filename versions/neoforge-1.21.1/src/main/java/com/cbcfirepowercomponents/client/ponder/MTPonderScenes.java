package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTItems;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class MTPonderScenes {
	private MTPonderScenes() {
	}

	public static void register(PonderSceneRegistrationHelper<ResourceLocation> registration) {
		PonderSceneRegistrationHelper<Item> helper = registration.withKeyFunction(BuiltInRegistries.ITEM::getKey);

		helper.forComponents(MTItems.COMPACT_CANNON_MOUNT.get())
			.addStoryBoard("mounts/compact", CannonMountScenes::compact);
		helper.forComponents(MTItems.COMPACT_AUTOCANNON_MOUNT.get())
			.addStoryBoard("mounts/compact", CannonMountScenes::compactAutocannon);
		helper.forComponents(MTItems.VERTICAL_COMPACT_CANNON_MOUNT.get())
			.addStoryBoard("mounts/vertical", CannonMountScenes::vertical);

		helper.forComponents(MTItems.LARGE_AUTOCANNON_BREECH.get(), MTItems.STEEL_LARGE_AUTOCANNON_BARREL.get(),
			MTItems.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get(), MTItems.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get(),
			MTItems.LARGE_AUTOCANNON_ROUND.get(), MTItems.LARGE_AUTOCANNON_HE_ROUND.get())
			.addStoryBoard("large_autocannon/single", CannonMountScenes::singleLargeAutocannon);
		helper.forComponents(MTItems.TWIN_LARGE_AUTOCANNON_BREECH.get(), MTItems.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get(),
			MTItems.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get())
			.addStoryBoard("large_autocannon/twin", CannonMountScenes::twinLargeAutocannon);

		helper.forComponents(MTItems.AUTOCANNON_AMMO_FEED.get(), MTItems.LARGE_AUTOCANNON_AMMO_BOX.get())
			.addStoryBoard("ammo/autocannon_feed", AmmunitionScenes::autocannonFeed);
		helper.forComponents(MTItems.CANNON_MAGAZINE_LOADER.get())
			.addStoryBoard("ammo/magazine_loader", AmmunitionScenes::magazineLoader);
		helper.forComponents(MTItems.READY_AMMUNITION_COMPARTMENT.get())
			.addStoryBoard("ammo/ready_compartment", AmmunitionScenes::readyCompartment);
		helper.forComponents(MTItems.CAROUSEL_AMMUNITION_RACK.get())
			.addStoryBoard("ammo/carousel", AmmunitionScenes::carousel);
		helper.forComponents(MTItems.SPENT_CASING_COLLECTOR.get())
			.addStoryBoard("logistics/spent_collector", AmmunitionScenes::spentCollector);

		helper.forComponents(MTItems.AUTOMATIC_CANNON_CONTROLLER.get())
			.addStoryBoard("control/automatic_controller", FireControlScenes::automaticController);
		helper.forComponents(MTItems.CANNON_LIMITER.get())
			.addStoryBoard("equipment/limiter", EquipmentScenes::limiter);
		helper.forComponents(Items.SPYGLASS)
			.addStoryBoard("equipment/rangefinding", EquipmentScenes::rangefinding);
	}
}
