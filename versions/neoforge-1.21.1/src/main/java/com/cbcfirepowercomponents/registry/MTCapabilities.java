package com.cbcfirepowercomponents.registry;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class MTCapabilities {

	public static void register(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MTBlockEntities.COMPACT_CANNON_MOUNT.get(),
			(blockEntity, side) -> blockEntity.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MTBlockEntities.AUTOCANNON_AMMO_FEED.get(),
			(blockEntity, side) -> blockEntity.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MTBlockEntities.LARGE_AUTOCANNON_AMMO_BOX.get(),
			(blockEntity, side) -> blockEntity.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MTBlockEntities.CANNON_MAGAZINE_LOADER.get(),
			(blockEntity, side) -> blockEntity.getItemHandler(side));
	}

}
