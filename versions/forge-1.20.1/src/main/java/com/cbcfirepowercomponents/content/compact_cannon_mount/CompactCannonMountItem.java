package com.cbcfirepowercomponents.content.compact_cannon_mount;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		boolean expanded = Screen.hasShiftDown();
		addHoldShift(expanded, tooltip);
		if (!expanded) {
			tooltip.add(Component.translatable(this.tooltipKey + ".summary")
				.withStyle(ChatFormatting.GRAY));
			return;
		}

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

	private static void addHoldShift(boolean expanded, List<Component> tooltip) {
		String[] holdDesc = Lang.translateDirect("tooltip.holdForDescription", "$").getString().split("\\$");
		if (holdDesc.length < 2)
			return;
		Component keyShift = Lang.translateDirect("tooltip.keyShift");
		MutableComponent line = Component.literal("");
		line.append(Component.literal(holdDesc[0]).withStyle(ChatFormatting.DARK_GRAY));
		line.append(keyShift.plainCopy().withStyle(expanded ? ChatFormatting.WHITE : ChatFormatting.GRAY));
		line.append(Component.literal(holdDesc[1]).withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(line);
	}

}
