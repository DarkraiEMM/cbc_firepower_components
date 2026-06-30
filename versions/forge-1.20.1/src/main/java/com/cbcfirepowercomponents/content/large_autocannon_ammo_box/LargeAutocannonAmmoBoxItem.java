package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;

public class LargeAutocannonAmmoBoxItem extends AutocannonAmmoContainerItem {
	private static final String TOOLTIP_KEY = "block.cbc_firepower_components.large_autocannon_ammo_box.tooltip";

	public LargeAutocannonAmmoBoxItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	public static ItemStack sanitizeForCbcMagazine(ItemStack stack) {
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".summary").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail1").withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail2").withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail3").withStyle(ChatFormatting.AQUA));
	}
}