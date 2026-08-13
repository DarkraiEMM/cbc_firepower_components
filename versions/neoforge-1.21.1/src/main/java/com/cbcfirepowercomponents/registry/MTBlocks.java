package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlock;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlock;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackStructuralBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactAutocannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.VerticalCompactCannonMountBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonMuzzleBrakeBlock;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlock;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlock;
import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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

	public static final DeferredHolder<Block, VerticalCompactCannonMountBlock> VERTICAL_COMPACT_CANNON_MOUNT =
		BLOCKS.register("vertical_compact_cannon_mount",
			() -> new VerticalCompactCannonMountBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(3.5f, 6.0f)
				.sound(SoundType.NETHERITE_BLOCK)
				.noOcclusion()
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
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_THICK_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_thick_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonMuzzleBrakeBlock> STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_large_autocannon_muzzle_brake",
		() -> new LargeAutocannonMuzzleBrakeBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL));

	public static final DeferredHolder<Block, LargeAutocannonBreechBlock> TWIN_LARGE_AUTOCANNON_BREECH = BLOCKS.register("twin_large_autocannon_breech",
		() -> new LargeAutocannonBreechBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL, true));

	public static final DeferredHolder<Block, LargeAutocannonBarrelBlock> STEEL_TWIN_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_twin_large_autocannon_barrel",
		() -> new LargeAutocannonBarrelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL, true));

	public static final DeferredHolder<Block, LargeAutocannonMuzzleBrakeBlock> STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_twin_large_autocannon_muzzle_brake",
		() -> new LargeAutocannonMuzzleBrakeBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false), CBCAutocannonMaterials.STEEL, true));

	public static final DeferredHolder<Block, CannonMagazineLoaderBlock> CANNON_MAGAZINE_LOADER = BLOCKS.register("cannon_magazine_loader",
		() -> new CannonMagazineLoaderBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.5f, 6.0f)
			.noOcclusion()
			.isRedstoneConductor((state, level, pos) -> false)));

	public static final DeferredHolder<Block, ReadyAmmunitionCompartmentBlock> READY_AMMUNITION_COMPARTMENT =
		BLOCKS.register("ready_ammunition_compartment", () -> new ReadyAmmunitionCompartmentBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

	public static final DeferredHolder<Block, SpentCasingCollectorBlock> SPENT_CASING_COLLECTOR =
		BLOCKS.register("spent_casing_collector", () -> new SpentCasingCollectorBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

	public static final DeferredHolder<Block, AutomaticCannonControllerBlock> AUTOMATIC_CANNON_CONTROLLER =
		BLOCKS.register("automatic_cannon_controller", () -> new AutomaticCannonControllerBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

	public static final DeferredHolder<Block, CarouselAmmunitionRackBlock> CAROUSEL_AMMUNITION_RACK =
		BLOCKS.register("carousel_ammunition_rack", () -> new CarouselAmmunitionRackBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(3.5f, 6.0f)
			.sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

	public static final DeferredHolder<Block, CarouselAmmunitionRackStructuralBlock> CAROUSEL_AMMUNITION_RACK_STRUCTURE =
		BLOCKS.register("carousel_ammunition_rack_structure", () -> new CarouselAmmunitionRackStructuralBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops()
				.strength(3.5f, 6.0f).sound(SoundType.NETHERITE_BLOCK).noOcclusion().noLootTable()));

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
	}

}
