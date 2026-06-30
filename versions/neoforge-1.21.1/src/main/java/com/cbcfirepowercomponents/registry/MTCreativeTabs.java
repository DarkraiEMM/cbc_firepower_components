package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTCreativeTabs {

	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FirepowerComponents.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
		.title(Component.translatable("itemGroup.cbc_firepower_components"))
		.icon(() -> MTItems.COMPACT_CANNON_MOUNT.get().getDefaultInstance())
		.displayItems((parameters, output) -> {
			output.accept(MTItems.COMPACT_CANNON_MOUNT.get());
			output.accept(MTItems.COMPACT_AUTOCANNON_MOUNT.get());
			output.accept(MTItems.AUTOCANNON_AMMO_FEED.get());
			output.accept(MTItems.LARGE_AUTOCANNON_AMMO_BOX.get());
			output.accept(MTItems.CANNON_MAGAZINE_LOADER.get());
			output.accept(MTItems.CANNON_LIMITER.get());
		})
		.build());

	public static void register(IEventBus bus) {
		TABS.register(bus);
	}

}
