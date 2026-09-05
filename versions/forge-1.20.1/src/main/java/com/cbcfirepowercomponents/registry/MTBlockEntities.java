package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlockEntity;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlockEntity;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MTBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FirepowerComponents.MOD_ID);

    public static final RegistryObject<BlockEntityType<CompactCannonMountBlockEntity>> COMPACT_CANNON_MOUNT = BLOCK_ENTITIES.register("compact_cannon_mount",
        () -> BlockEntityType.Builder.of(CompactCannonMountBlockEntity::new, MTBlocks.COMPACT_CANNON_MOUNT.get(),
            MTBlocks.COMPACT_AUTOCANNON_MOUNT.get(), MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get()).build(null));

    public static final RegistryObject<BlockEntityType<AutocannonAmmoFeedBlockEntity>> AUTOCANNON_AMMO_FEED = BLOCK_ENTITIES.register("autocannon_ammo_feed",
        () -> BlockEntityType.Builder.of(AutocannonAmmoFeedBlockEntity::new, MTBlocks.AUTOCANNON_AMMO_FEED.get()).build(null));

    public static final RegistryObject<BlockEntityType<LargeAutocannonAmmoBoxBlockEntity>> LARGE_AUTOCANNON_AMMO_BOX = BLOCK_ENTITIES.register("large_autocannon_ammo_box",
        () -> BlockEntityType.Builder.of(LargeAutocannonAmmoBoxBlockEntity::new, MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<LargeAutocannonBlockEntity>> LARGE_AUTOCANNON = BLOCK_ENTITIES.register("large_autocannon",
        () -> BlockEntityType.Builder.of(LargeAutocannonBlockEntity::new,
            MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get(), MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get(),
            MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get(), MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get(),
            MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get()).build(null));

    public static final RegistryObject<BlockEntityType<LargeAutocannonBreechBlockEntity>> LARGE_AUTOCANNON_BREECH = BLOCK_ENTITIES.register("large_autocannon_breech",
        () -> BlockEntityType.Builder.of(LargeAutocannonBreechBlockEntity::new,
            MTBlocks.LARGE_AUTOCANNON_BREECH.get(), MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get()).build(null));

    public static final RegistryObject<BlockEntityType<CannonMagazineLoaderBlockEntity>> CANNON_MAGAZINE_LOADER = BLOCK_ENTITIES.register("cannon_magazine_loader",
        () -> BlockEntityType.Builder.of(CannonMagazineLoaderBlockEntity::new, MTBlocks.CANNON_MAGAZINE_LOADER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ReadyAmmunitionCompartmentBlockEntity>> READY_AMMUNITION_COMPARTMENT =
        BLOCK_ENTITIES.register("ready_ammunition_compartment", () -> BlockEntityType.Builder.of(
            ReadyAmmunitionCompartmentBlockEntity::new, MTBlocks.READY_AMMUNITION_COMPARTMENT.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpentCasingCollectorBlockEntity>> SPENT_CASING_COLLECTOR =
        BLOCK_ENTITIES.register("spent_casing_collector", () -> BlockEntityType.Builder.of(
            SpentCasingCollectorBlockEntity::new, MTBlocks.SPENT_CASING_COLLECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<AutomaticCannonControllerBlockEntity>> AUTOMATIC_CANNON_CONTROLLER =
        BLOCK_ENTITIES.register("automatic_cannon_controller", () -> BlockEntityType.Builder.of(
            AutomaticCannonControllerBlockEntity::new, MTBlocks.AUTOMATIC_CANNON_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CarouselAmmunitionRackBlockEntity>> CAROUSEL_AMMUNITION_RACK =
        BLOCK_ENTITIES.register("carousel_ammunition_rack", () -> BlockEntityType.Builder.of(
            CarouselAmmunitionRackBlockEntity::new, MTBlocks.CAROUSEL_AMMUNITION_RACK.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

}
