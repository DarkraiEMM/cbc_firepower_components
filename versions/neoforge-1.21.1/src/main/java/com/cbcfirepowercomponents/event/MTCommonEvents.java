package com.cbcfirepowercomponents.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class MTCommonEvents {
	private MTCommonEvents() {}

	public static void onTooltip(ItemTooltipEvent event) {
		if (event.getItemStack().is(Items.SPYGLASS))
			event.getToolTip().add(Component.translatable(
				"item.cbc_firepower_components.spyglass_rangefinder.tooltip").withStyle(ChatFormatting.DARK_AQUA));
	}
}
