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
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlock;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonMuzzleBrakeBlock;
import com.cbcfirepowercomponents.content.machine_gun_shield.SleeveMachineGunShieldBlock;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlock;
import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

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

    public static final RegistryObject<VerticalCompactCannonMountBlock> VERTICAL_COMPACT_CANNON_MOUNT =
        BLOCKS.register("vertical_compact_cannon_mount", () -> new VerticalCompactCannonMountBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f, 6.0f)
            .sound(SoundType.NETHERITE_BLOCK).noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)));

    public static final RegistryObject<AutocannonAmmoFeedBlock> AUTOCANNON_AMMO_FEED = BLOCKS.register("autocannon_ammo_feed",
        () -> new AutocannonAmmoFeedBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(2.5f, 6.0f)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)));

    public static final RegistryObject<LargeAutocannonAmmoBoxBlock> LARGE_AUTOCANNON_AMMO_BOX = BLOCKS.register("large_autocannon_ammo_box",
        () -> new LargeAutocannonAmmoBoxBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(3.0f, 6.0f)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)));

    public static final RegistryObject<LargeAutocannonBreechBlock> LARGE_AUTOCANNON_BREECH = BLOCKS.register("large_autocannon_breech",
        () -> new LargeAutocannonBreechBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL));
    public static final RegistryObject<LargeAutocannonBarrelBlock> STEEL_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_large_autocannon_barrel",
        () -> new LargeAutocannonBarrelBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL));
    public static final RegistryObject<LargeAutocannonBarrelBlock> STEEL_THICK_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_thick_large_autocannon_barrel",
        () -> new LargeAutocannonBarrelBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL));
    public static final RegistryObject<LargeAutocannonMuzzleBrakeBlock> STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_large_autocannon_muzzle_brake",
        () -> new LargeAutocannonMuzzleBrakeBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL));
    public static final RegistryObject<LargeAutocannonBreechBlock> TWIN_LARGE_AUTOCANNON_BREECH = BLOCKS.register("twin_large_autocannon_breech",
        () -> new LargeAutocannonBreechBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL, true));
    public static final RegistryObject<LargeAutocannonBarrelBlock> STEEL_TWIN_LARGE_AUTOCANNON_BARREL = BLOCKS.register("steel_twin_large_autocannon_barrel",
        () -> new LargeAutocannonBarrelBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL, true));
    public static final RegistryObject<LargeAutocannonMuzzleBrakeBlock> STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE = BLOCKS.register("steel_twin_large_autocannon_muzzle_brake",
        () -> new LargeAutocannonMuzzleBrakeBlock(largeAutocannonProperties(), CBCAutocannonMaterials.STEEL, true));

    private static BlockBehaviour.Properties largeAutocannonProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .strength(3.5f, 6.0f).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false);
    }

    public static final RegistryObject<CannonMagazineLoaderBlock> CANNON_MAGAZINE_LOADER = BLOCKS.register("cannon_magazine_loader",
        () -> new CannonMagazineLoaderBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(2.5f, 6.0f)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)));

    public static final RegistryObject<ReadyAmmunitionCompartmentBlock> READY_AMMUNITION_COMPARTMENT =
        BLOCKS.register("ready_ammunition_compartment", () -> new ReadyAmmunitionCompartmentBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f, 6.0f)
            .sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    public static final RegistryObject<SpentCasingCollectorBlock> SPENT_CASING_COLLECTOR =
        BLOCKS.register("spent_casing_collector", () -> new SpentCasingCollectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.5f, 6.0f)
            .sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    public static final RegistryObject<AutomaticCannonControllerBlock> AUTOMATIC_CANNON_CONTROLLER =
        BLOCKS.register("automatic_cannon_controller", () -> new AutomaticCannonControllerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.5f, 6.0f)
            .sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    public static final RegistryObject<CarouselAmmunitionRackBlock> CAROUSEL_AMMUNITION_RACK =
        BLOCKS.register("carousel_ammunition_rack", () -> new CarouselAmmunitionRackBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(3.5f, 6.0f)
            .sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    public static final RegistryObject<CarouselAmmunitionRackStructuralBlock> CAROUSEL_AMMUNITION_RACK_STRUCTURE =
        BLOCKS.register("carousel_ammunition_rack_structure", () -> new CarouselAmmunitionRackStructuralBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops()
                .strength(3.5f, 6.0f).sound(SoundType.NETHERITE_BLOCK).noOcclusion().noLootTable()));

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
