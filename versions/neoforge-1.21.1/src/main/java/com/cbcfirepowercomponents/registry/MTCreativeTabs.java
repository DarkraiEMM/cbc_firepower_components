package com.cbcfirepowercomponents.registry;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.large_autocannon_ammo.LargeAutocannonRoundItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
			output.accept(MTItems.VERTICAL_COMPACT_CANNON_MOUNT.get());
			output.accept(MTItems.AUTOCANNON_AMMO_FEED.get());
			output.accept(MTItems.LARGE_AUTOCANNON_AMMO_BOX.get());
			output.accept(MTItems.LARGE_AUTOCANNON_BREECH.get());
			output.accept(MTItems.STEEL_LARGE_AUTOCANNON_BARREL.get());
			output.accept(MTItems.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get());
			output.accept(MTItems.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
			output.accept(MTItems.TWIN_LARGE_AUTOCANNON_BREECH.get());
			output.accept(MTItems.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get());
			output.accept(MTItems.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
			output.accept(MTItems.LARGE_AUTOCANNON_ROUND.get());
			output.accept(createTracerRound(MTItems.LARGE_AUTOCANNON_ROUND.get().getDefaultInstance()));
			output.accept(MTItems.LARGE_AUTOCANNON_HE_ROUND.get());
			output.accept(createTracerRound(MTItems.LARGE_AUTOCANNON_HE_ROUND.get().getDefaultInstance()));
			output.accept(MTItems.CANNON_MAGAZINE_LOADER.get());
			output.accept(MTItems.READY_AMMUNITION_COMPARTMENT.get());
			output.accept(MTItems.SPENT_CASING_COLLECTOR.get());
			output.accept(MTItems.AUTOMATIC_CANNON_CONTROLLER.get());
			output.accept(MTItems.CAROUSEL_AMMUNITION_RACK.get());
			output.accept(MTItems.CANNON_LIMITER.get());
		})
		.build());

	private static ItemStack createTracerRound(ItemStack stack) {
		((LargeAutocannonRoundItem) stack.getItem()).setTracer(stack, true);
		return stack;
	}

	public static void register(IEventBus bus) {
		TABS.register(bus);
	}

}
