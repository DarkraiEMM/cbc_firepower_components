package com.cbcfirepowercomponents.content;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.compact_cannon_mount.input.MountedWeaponInputContext;
import com.cbcfirepowercomponents.content.compact_cannon_mount.input.MountedWeaponInputStrategies;
import com.cbcfirepowercomponents.registry.MTArmInteractionPointTypes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;

public final class MountedCannonAmmunitionTarget {
	private MountedCannonAmmunitionTarget() {}

	public static boolean isMountedTarget(@Nullable BlockEntity blockEntity) {
		return context(blockEntity) != null;
	}

	public static ItemStack insert(@Nullable BlockEntity blockEntity, ItemStack stack, boolean simulate) {
		MountedWeaponInputContext context = context(blockEntity);
		if (context == null)
			return stack;
		if (blockEntity != null && blockEntity.getLevel() != null) {
			// Use CBC's canonical arm-loading entry point first. CBCMS, CBCAT and
			// CBC Neo Warfare all inject their own cannon loaders into this method,
			// so direct rack-to-mount transfer receives the same compatibility as
			// a mechanical arm targeting an original CBC cannon mount.
			CannonMountPoint bridge = new CannonMountPoint(
				MTArmInteractionPointTypes.COMPACT_CANNON_MOUNT.get(),
				blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
			ItemStack result = bridge.getInsertedResultAndDoSomething(
				stack, simulate, context.cannon(), context.entity());
			if (CannonAmmunitionHelper.wasAccepted(stack, result))
				return result;
		}
		return MountedWeaponInputStrategies.insert(context, stack, simulate);
	}

	@Nullable
	private static MountedWeaponInputContext context(@Nullable BlockEntity blockEntity) {
		PitchOrientedContraptionEntity entity;
		if (blockEntity instanceof CompactCannonMountBlockEntity compact)
			entity = compact.getContraption();
		else if (blockEntity instanceof CannonMountBlockEntity mount)
			entity = mount.getContraption();
		else if (blockEntity instanceof FixedCannonMountBlockEntity mount)
			entity = mount.getContraption();
		else
			return null;
		if (entity == null || !(entity.getContraption() instanceof AbstractMountedCannonContraption cannon))
			return null;
		return new MountedWeaponInputContext(entity, cannon);
	}
}
