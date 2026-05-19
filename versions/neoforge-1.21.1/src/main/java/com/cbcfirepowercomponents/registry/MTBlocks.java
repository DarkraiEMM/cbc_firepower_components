package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactAutocannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

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
