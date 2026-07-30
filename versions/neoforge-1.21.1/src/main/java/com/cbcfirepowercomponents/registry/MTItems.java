package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.SimpleTooltipBlockItem;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterItem;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountItem;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonRoundItem;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlockItem;

public class MTItems {

	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FirepowerComponents.MOD_ID);

	public static final DeferredHolder<Item, BlockItem> COMPACT_CANNON_MOUNT = ITEMS.register("compact_cannon_mount",
		() -> new CompactCannonMountItem(MTBlocks.COMPACT_CANNON_MOUNT.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> COMPACT_AUTOCANNON_MOUNT = ITEMS.register("compact_autocannon_mount",
		() -> new CompactCannonMountItem(MTBlocks.COMPACT_AUTOCANNON_MOUNT.get(), new Item.Properties(),
			"block.cbc_firepower_components.compact_autocannon_mount.tooltip"));

	public static final DeferredHolder<Item, BlockItem> VERTICAL_COMPACT_CANNON_MOUNT = ITEMS.register("vertical_compact_cannon_mount",
		() -> new CompactCannonMountItem(MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get(), new Item.Properties(),
			"block.cbc_firepower_components.vertical_compact_cannon_mount.tooltip"));

	public static final DeferredHolder<Item, BlockItem> AUTOCANNON_AMMO_FEED = ITEMS.register("autocannon_ammo_feed",
		() -> new SimpleTooltipBlockItem(MTBlocks.AUTOCANNON_AMMO_FEED.get(), new Item.Properties(),
			"block.cbc_firepower_components.autocannon_ammo_feed.tooltip", 2));

	public static final DeferredHolder<Item, BlockItem> LARGE_AUTOCANNON_AMMO_BOX = ITEMS.register("large_autocannon_ammo_box",
		() -> new LargeAutocannonAmmoBoxItem(MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> LARGE_AUTOCANNON_BREECH = ITEMS.register("large_autocannon_breech",
		() -> new AutocannonBlockItem<>(MTBlocks.LARGE_AUTOCANNON_BREECH.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> STEEL_LARGE_AUTOCANNON_BARREL = ITEMS.register("steel_large_autocannon_barrel",
		() -> new AutocannonBlockItem<>(MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> STEEL_THICK_LARGE_AUTOCANNON_BARREL = ITEMS.register("steel_thick_large_autocannon_barrel",
		() -> new AutocannonBlockItem<>(MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE = ITEMS.register("steel_large_autocannon_muzzle_brake",
		() -> new AutocannonBlockItem<>(MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> TWIN_LARGE_AUTOCANNON_BREECH = ITEMS.register("twin_large_autocannon_breech",
		() -> new AutocannonBlockItem<>(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> STEEL_TWIN_LARGE_AUTOCANNON_BARREL = ITEMS.register("steel_twin_large_autocannon_barrel",
		() -> new AutocannonBlockItem<>(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE = ITEMS.register("steel_twin_large_autocannon_muzzle_brake",
		() -> new AutocannonBlockItem<>(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get(), new Item.Properties()));

	public static final DeferredHolder<Item, Item> LARGE_AUTOCANNON_ROUND = ITEMS.register("large_autocannon_round",
		() -> new LargeAutocannonRoundItem(new Item.Properties(), LargeAutocannonRoundItem.Kind.ARMOR_PIERCING));

	public static final DeferredHolder<Item, Item> LARGE_AUTOCANNON_HE_ROUND = ITEMS.register("large_autocannon_he_round",
		() -> new LargeAutocannonRoundItem(new Item.Properties(), LargeAutocannonRoundItem.Kind.HIGH_EXPLOSIVE));

	public static final DeferredHolder<Item, BlockItem> CANNON_MAGAZINE_LOADER = ITEMS.register("cannon_magazine_loader",
		() -> new SimpleTooltipBlockItem(MTBlocks.CANNON_MAGAZINE_LOADER.get(), new Item.Properties(),
			"block.cbc_firepower_components.cannon_magazine_loader.tooltip", 4));

	public static final DeferredHolder<Item, Item> CANNON_LIMITER = ITEMS.register("cannon_limiter",
		() -> new CannonLimiterItem(new Item.Properties().stacksTo(1)));

	public static final DeferredHolder<Item, BlockItem> READY_AMMUNITION_COMPARTMENT = ITEMS.register("ready_ammunition_compartment",
		() -> new SimpleTooltipBlockItem(MTBlocks.READY_AMMUNITION_COMPARTMENT.get(), new Item.Properties(),
			"block.cbc_firepower_components.ready_ammunition_compartment.tooltip", 4));

	public static final DeferredHolder<Item, BlockItem> SPENT_CASING_COLLECTOR = ITEMS.register("spent_casing_collector",
		() -> new SimpleTooltipBlockItem(MTBlocks.SPENT_CASING_COLLECTOR.get(), new Item.Properties(),
			"block.cbc_firepower_components.spent_casing_collector.tooltip", 3));

	public static final DeferredHolder<Item, BlockItem> AUTOMATIC_CANNON_CONTROLLER = ITEMS.register("automatic_cannon_controller",
		() -> new SimpleTooltipBlockItem(MTBlocks.AUTOMATIC_CANNON_CONTROLLER.get(), new Item.Properties(),
			"block.cbc_firepower_components.automatic_cannon_controller.tooltip", 4));

	public static final DeferredHolder<Item, BlockItem> CAROUSEL_AMMUNITION_RACK = ITEMS.register("carousel_ammunition_rack",
		() -> new SimpleTooltipBlockItem(MTBlocks.CAROUSEL_AMMUNITION_RACK.get(), new Item.Properties(),
			"block.cbc_firepower_components.carousel_ammunition_rack.tooltip", 4));

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
	}

}
