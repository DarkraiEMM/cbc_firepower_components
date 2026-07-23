package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactAutocannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonMuzzleBrakeBlock;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

public class MTBlocks {

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(FirepowerComponents.MOD_ID);

	public static final DeferredHolder<Block, CompactCannonMountBlock> COMPACT_CANNON_MOUNT = BLOCKS.register("compact_cannon_mount",
		() -> new CompactCannonMountBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.0f, 6.0f)));

	public static final DeferredHolder<Block, CompactAutocannonMountBlock> COMPACT_AUTOCANNON_MOUNT = BLOCKS.register("compact_autocannon_mount",
		() -> new CompactAutocannonMountBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.0f, 6.0f)
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final DeferredHolder<Block, AutocannonAmmoFeedBlock> AUTOCANNON_AMMO_FEED = BLOCKS.register("autocannon_ammo_feed",
		() -> new AutocannonAmmoFeedBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final DeferredHolder<Block, LargeAutocannonAmmoBoxBlock> LARGE_AUTOCANNON_AMMO_BOX = BLOCKS.register("large_autocannon_ammo_box",
		() -> new LargeAutocannonAmmoBoxBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.0f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final DeferredHolder<Block, LargeAutocannonBreechBlock> LARGE_AUTOCANNON_BREECH = BLOCKS.register("large_autocannon_breech",
		() -> new LargeAutocannonBreechBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_THICK_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_thick_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonMuzzleBrakeBlock> STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_large_autocannon_muzzle_brake",
		() -> new LargeAutocannonMuzzleBrakeBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBreechBlock> TWIN_LARGE_AUTOCANNON_BREECH = BLOCKS.register("twin_large_autocannon_breech",
		() -> new LargeAutocannonBreechBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_TWIN_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_twin_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonMuzzleBrakeBlock> STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_twin_large_autocannon_muzzle_brake",
		() -> new LargeAutocannonMuzzleBrakeBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, CannonMagazineLoaderBlock> CANNON_MAGAZINE_LOADER = BLOCKS.register("cannon_magazine_loader",
		() -> new CannonMagazineLoaderBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
	}

}
