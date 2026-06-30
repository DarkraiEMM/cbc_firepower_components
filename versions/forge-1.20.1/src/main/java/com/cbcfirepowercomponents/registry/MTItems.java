package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.CleanBlockItem;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterItem;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountItem;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxItem;
import com.cbcfirepowercomponents.content.machine_gun_shield.SleeveMachineGunShieldItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MTItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FirepowerComponents.MOD_ID);

    public static final RegistryObject<BlockItem> COMPACT_CANNON_MOUNT = ITEMS.register("compact_cannon_mount",
        () -> new CompactCannonMountItem(MTBlocks.COMPACT_CANNON_MOUNT.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> COMPACT_AUTOCANNON_MOUNT = ITEMS.register("compact_autocannon_mount",
        () -> new CompactCannonMountItem(MTBlocks.COMPACT_AUTOCANNON_MOUNT.get(), new Item.Properties(),
            "block.cbc_firepower_components.compact_autocannon_mount.tooltip"));

    public static final RegistryObject<BlockItem> AUTOCANNON_AMMO_FEED = ITEMS.register("autocannon_ammo_feed",
        () -> new CleanBlockItem(MTBlocks.AUTOCANNON_AMMO_FEED.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> LARGE_AUTOCANNON_AMMO_BOX = ITEMS.register("large_autocannon_ammo_box",
        () -> new LargeAutocannonAmmoBoxItem(MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> CANNON_MAGAZINE_LOADER = ITEMS.register("cannon_magazine_loader",
        () -> new CleanBlockItem(MTBlocks.CANNON_MAGAZINE_LOADER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CANNON_LIMITER = ITEMS.register("cannon_limiter",
        () -> new CannonLimiterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<BlockItem> SLEEVE_MACHINE_GUN_SHIELD = ITEMS.register("sleeve_machine_gun_shield",
        () -> new SleeveMachineGunShieldItem(MTBlocks.SLEEVE_MACHINE_GUN_SHIELD.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

}