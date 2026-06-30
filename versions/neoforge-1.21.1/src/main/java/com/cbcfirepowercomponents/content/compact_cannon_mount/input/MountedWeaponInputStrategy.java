package com.cbcfirepowercomponents.content.compact_cannon_mount.input;

import net.minecraft.world.item.ItemStack;

public interface MountedWeaponInputStrategy {
	boolean canInsert(MountedWeaponInputContext context, ItemStack stack);

	ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate);
}
