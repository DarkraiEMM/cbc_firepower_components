package com.cbcfirepowercomponents.content.compact_cannon_mount;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class CompactCannonMountItem extends BlockItem {

	private final String tooltipKey;

	public CompactCannonMountItem(Block block, Item.Properties properties) {
		this(block, properties, "block.cbc_firepower_components.compact_cannon_mount.tooltip");
	}

	public CompactCannonMountItem(Block block, Item.Properties properties, String tooltipKey) {
		super(block, properties);
		this.tooltipKey = tooltipKey;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable(this.tooltipKey + ".summary")
			.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable(this.tooltipKey + ".left")
			.withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable(this.tooltipKey + ".pitch")
			.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable(this.tooltipKey + ".yaw")
			.withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.translatable(this.tooltipKey + ".fire")
			.withStyle(ChatFormatting.RED));
		tooltip.add(Component.translatable(this.tooltipKey + ".assembly")
			.withStyle(ChatFormatting.LIGHT_PURPLE));
		tooltip.add(Component.translatable(this.tooltipKey + ".loading")
			.withStyle(ChatFormatting.GREEN));
	}

}
