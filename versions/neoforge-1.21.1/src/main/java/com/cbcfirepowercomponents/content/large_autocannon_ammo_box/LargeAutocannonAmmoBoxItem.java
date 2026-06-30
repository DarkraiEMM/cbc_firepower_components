package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;

public class LargeAutocannonAmmoBoxItem extends AutocannonAmmoContainerItem {
	private static final String TOOLTIP_KEY = "block.cbc_firepower_components.large_autocannon_ammo_box.tooltip";

	public LargeAutocannonAmmoBoxItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	public static ItemStack sanitizeForCbcMagazine(ItemStack stack) {
		if (!(stack.getItem() instanceof LargeAutocannonAmmoBoxItem))
			return stack;
		clampAmmoComponent(stack, CBCDataComponents.AMMO);
		clampAmmoComponent(stack, CBCDataComponents.TRACERS);
		return stack;
	}

	private static void clampAmmoComponent(ItemStack stack, DataComponentType<ItemContainerContents> component) {
		ItemStack ammo = stack.getOrDefault(component, ItemContainerContents.EMPTY).copyOne();
		if (ammo.isEmpty()) {
			stack.set(component, ItemContainerContents.EMPTY);
			return;
		}
		if (ammo.getCount() > LargeAutocannonAmmoBoxBlockEntity.MAX_SERIALIZED_STACK_COUNT)
			ammo.setCount(LargeAutocannonAmmoBoxBlockEntity.MAX_SERIALIZED_STACK_COUNT);
		stack.set(component, ItemContainerContents.fromItems(List.of(ammo)));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (!level.isClientSide)
			sanitizeForCbcMagazine(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".summary").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail1").withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail2").withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable(TOOLTIP_KEY + ".detail3").withStyle(ChatFormatting.AQUA));
	}
}