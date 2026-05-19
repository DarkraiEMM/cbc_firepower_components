package com.cbcfirepowercomponents.content;

import java.util.List;

import net.minecraft.ChatFormatting;
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
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable(this.tooltipKey + ".summary").withStyle(ChatFormatting.GRAY));
		for (int i = 1; i <= this.detailLines; ++i)
			tooltip.add(Component.translatable(this.tooltipKey + ".detail" + i).withStyle(ChatFormatting.AQUA));
	}
}
