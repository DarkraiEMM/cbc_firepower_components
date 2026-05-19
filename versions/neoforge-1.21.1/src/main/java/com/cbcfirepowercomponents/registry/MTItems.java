package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.SimpleTooltipBlockItem;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTItems {

	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FirepowerComponents.MOD_ID);

	public static final DeferredHolder<Item, BlockItem> COMPACT_CANNON_MOUNT = ITEMS.register("compact_cannon_mount",
		() -> new CompactCannonMountItem(MTBlocks.COMPACT_CANNON_MOUNT.get(), new Item.Properties()));

	public static final DeferredHolder<Item, BlockItem> COMPACT_AUTOCANNON_MOUNT = ITEMS.register("compact_autocannon_mount",
		() -> new CompactCannonMountItem(MTBlocks.COMPACT_AUTOCANNON_MOUNT.get(), new Item.Properties(),
			"block.cbc_firepower_components.compact_autocannon_mount.tooltip"));

	public static final DeferredHolder<Item, BlockItem> AUTOCANNON_AMMO_FEED = ITEMS.register("autocannon_ammo_feed",
		() -> new SimpleTooltipBlockItem(MTBlocks.AUTOCANNON_AMMO_FEED.get(), new Item.Properties(),
			"block.cbc_firepower_components.autocannon_ammo_feed.tooltip", 2));

	public static final DeferredHolder<Item, BlockItem> CANNON_MAGAZINE_LOADER = ITEMS.register("cannon_magazine_loader",
		() -> new SimpleTooltipBlockItem(MTBlocks.CANNON_MAGAZINE_LOADER.get(), new Item.Properties(),
			"block.cbc_firepower_components.cannon_magazine_loader.tooltip", 4));

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
	}

}
