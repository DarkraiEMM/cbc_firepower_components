package com.cbcfirepowercomponents.content;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
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
	public void verifyComponentsAfterLoad(ItemStack stack) {
		super.verifyComponentsAfterLoad(stack);
		stack.remove(DataComponents.BLOCK_STATE);
		stack.remove(DataComponents.BLOCK_ENTITY_DATA);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
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
