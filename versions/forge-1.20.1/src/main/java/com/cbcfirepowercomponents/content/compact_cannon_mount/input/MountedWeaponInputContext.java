package com.cbcfirepowercomponents.content.compact_cannon_mount.input;

import javax.annotation.Nullable;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public record MountedWeaponInputContext(PitchOrientedContraptionEntity entity,
										AbstractMountedCannonContraption cannon) {

	@Nullable
	public IItemHandler itemHandler() {
		return this.entity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
	}

	@Nullable
	public ItemCannon itemCannon() {
		return this.cannon instanceof ItemCannon itemCannon ? itemCannon : null;
	}
}
