package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonAPProjectile;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonHEProjectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MTEntityTypes {
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
		DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FirepowerComponents.MOD_ID);

	public static final RegistryObject<EntityType<LargeAutocannonAPProjectile>> LARGE_AUTOCANNON_AP_PROJECTILE =
		ENTITY_TYPES.register("large_autocannon_ap_projectile", () -> EntityType.Builder
			.<LargeAutocannonAPProjectile>of(LargeAutocannonAPProjectile::new, MobCategory.MISC)
			.sized(0.2f, 0.2f).clientTrackingRange(16).updateInterval(1)
			.build("large_autocannon_ap_projectile"));

	public static final RegistryObject<EntityType<LargeAutocannonHEProjectile>> LARGE_AUTOCANNON_HE_PROJECTILE =
		ENTITY_TYPES.register("large_autocannon_he_projectile", () -> EntityType.Builder
			.<LargeAutocannonHEProjectile>of(LargeAutocannonHEProjectile::new, MobCategory.MISC)
			.sized(0.2f, 0.2f).clientTrackingRange(16).updateInterval(1)
			.build("large_autocannon_he_projectile"));

	private MTEntityTypes() {}

	public static void register(IEventBus bus) {
		ENTITY_TYPES.register(bus);
	}
}
