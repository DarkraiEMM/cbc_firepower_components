package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactAutocannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.machine_gun_shield.SleeveMachineGunShieldBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MTBlocks {

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FirepowerComponents.MOD_ID);

	public static final RegistryObject<CompactCannonMountBlock> COMPACT_CANNON_MOUNT = BLOCKS.register("compact_cannon_mount",
		() -> new CompactCannonMountBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.0f, 6.0f)));

	public static final RegistryObject<CompactAutocannonMountBlock> COMPACT_AUTOCANNON_MOUNT = BLOCKS.register("compact_autocannon_mount",
		() -> new CompactAutocannonMountBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.0f, 6.0f)
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final RegistryObject<AutocannonAmmoFeedBlock> AUTOCANNON_AMMO_FEED = BLOCKS.register("autocannon_ammo_feed",
		() -> new AutocannonAmmoFeedBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final RegistryObject<CannonMagazineLoaderBlock> CANNON_MAGAZINE_LOADER = BLOCKS.register("cannon_magazine_loader",
		() -> new CannonMagazineLoaderBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final RegistryObject<SleeveMachineGunShieldBlock> SLEEVE_MACHINE_GUN_SHIELD = BLOCKS.register("sleeve_machine_gun_shield",
		() -> new SleeveMachineGunShieldBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.0f, 6.0f)
			.noOcclusion()
			.noCollission()));

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
	}

}
