package com.cbcfirepowercomponents.content;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class SimpleTooltipBlockItem extends BlockItem {
	private final String tooltipKey;
	private final int detailLines;

	public SimpleTooltipBlockItem(Block block, Item.Properties properties, String tooltipKey, int detailLines) {
		super(block, properties);
		this.tooltipKey = tooltipKey;
		this.detailLines = detailLines;
	}

	@Override
	public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level,
		List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable(this.tooltipKey + ".summary").withStyle(ChatFormatting.GRAY));
		if (Screen.hasShiftDown()) {
			for (int i = 1; i <= this.detailLines; ++i)
				tooltip.add(Component.translatable(this.tooltipKey + ".detail" + i).withStyle(ChatFormatting.AQUA));
		} else {
			tooltip.add(Component.translatable("tooltip.cbc_firepower_components.hold_shift",
				Component.literal("Shift").withStyle(ChatFormatting.YELLOW))
				.withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
