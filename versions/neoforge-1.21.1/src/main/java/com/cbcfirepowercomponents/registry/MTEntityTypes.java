package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonAPProjectile;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonHEProjectile;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTEntityTypes {
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, FirepowerComponents.MOD_ID);

	public static final DeferredHolder<EntityType<?>, EntityType<LargeAutocannonAPProjectile>> LARGE_AUTOCANNON_AP_PROJECTILE =
		ENTITY_TYPES.register("large_autocannon_ap_projectile", () -> EntityType.Builder
			.<LargeAutocannonAPProjectile>of(LargeAutocannonAPProjectile::new, MobCategory.MISC)
			.sized(0.2f, 0.2f)
			.clientTrackingRange(16)
			.updateInterval(1)
			.build("large_autocannon_ap_projectile"));

	public static final DeferredHolder<EntityType<?>, EntityType<LargeAutocannonHEProjectile>> LARGE_AUTOCANNON_HE_PROJECTILE =
		ENTITY_TYPES.register("large_autocannon_he_projectile", () -> EntityType.Builder
			.<LargeAutocannonHEProjectile>of(LargeAutocannonHEProjectile::new, MobCategory.MISC)
			.sized(0.2f, 0.2f)
			.clientTrackingRange(16)
			.updateInterval(1)
			.build("large_autocannon_he_projectile"));

	public static void register(IEventBus bus) {
		ENTITY_TYPES.register(bus);
	}
}
