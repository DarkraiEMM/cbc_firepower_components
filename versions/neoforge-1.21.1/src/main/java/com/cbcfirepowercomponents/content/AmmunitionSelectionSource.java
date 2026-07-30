package com.cbcfirepowercomponents.content;

import java.util.List;

import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface AmmunitionSelectionSource {
	List<ReadyAmmunitionCompartmentBlockEntity.RoundType> getAvailableRoundTypes();
	boolean selectType(ItemStack projectile, ItemStack propellant);
	void selectNextType();
	BlockPos getBlockPos();
}
