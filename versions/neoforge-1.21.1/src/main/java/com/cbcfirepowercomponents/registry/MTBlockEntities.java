package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlockEntity;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTBlockEntities {

	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FirepowerComponents.MOD_ID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompactCannonMountBlockEntity>> COMPACT_CANNON_MOUNT = BLOCK_ENTITIES.register("compact_cannon_mount",
		() -> BlockEntityType.Builder.of(CompactCannonMountBlockEntity::new, MTBlocks.COMPACT_CANNON_MOUNT.get(),
			MTBlocks.COMPACT_AUTOCANNON_MOUNT.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutocannonAmmoFeedBlockEntity>> AUTOCANNON_AMMO_FEED = BLOCK_ENTITIES.register("autocannon_ammo_feed",
		() -> BlockEntityType.Builder.of(AutocannonAmmoFeedBlockEntity::new, MTBlocks.AUTOCANNON_AMMO_FEED.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeAutocannonAmmoBoxBlockEntity>> LARGE_AUTOCANNON_AMMO_BOX = BLOCK_ENTITIES.register("large_autocannon_ammo_box",
		() -> BlockEntityType.Builder.of(LargeAutocannonAmmoBoxBlockEntity::new, MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeAutocannonBlockEntity>> LARGE_AUTOCANNON = BLOCK_ENTITIES.register("large_autocannon",
		() -> BlockEntityType.Builder.of(LargeAutocannonBlockEntity::new,
			MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get(),
			MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get(),
			MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get(),
			MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get(),
			MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeAutocannonBreechBlockEntity>> LARGE_AUTOCANNON_BREECH = BLOCK_ENTITIES.register("large_autocannon_breech",
		() -> BlockEntityType.Builder.of(LargeAutocannonBreechBlockEntity::new,
			MTBlocks.LARGE_AUTOCANNON_BREECH.get(),
			MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CannonMagazineLoaderBlockEntity>> CANNON_MAGAZINE_LOADER = BLOCK_ENTITIES.register("cannon_magazine_loader",
		() -> BlockEntityType.Builder.of(CannonMagazineLoaderBlockEntity::new, MTBlocks.CANNON_MAGAZINE_LOADER.get()).build(null));

	public static void register(IEventBus bus) {
		BLOCK_ENTITIES.register(bus);
	}

}
