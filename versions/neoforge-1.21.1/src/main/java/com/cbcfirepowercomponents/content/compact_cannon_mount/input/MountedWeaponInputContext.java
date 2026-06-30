package com.cbcfirepowercomponents.content.compact_cannon_mount.input;

import javax.annotation.Nullable;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public record MountedWeaponInputContext(PitchOrientedContraptionEntity entity,
										AbstractMountedCannonContraption cannon) {

	@Nullable
	public IItemHandler itemHandler() {
		return this.entity.getCapability(Capabilities.ItemHandler.ENTITY);
	}

	@Nullable
	public ItemCannon itemCannon() {
		return this.cannon instanceof ItemCannon itemCannon ? itemCannon : null;
	}
}
