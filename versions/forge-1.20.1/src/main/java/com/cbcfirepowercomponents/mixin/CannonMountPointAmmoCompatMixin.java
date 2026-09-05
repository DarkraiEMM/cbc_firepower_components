package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.input.MountedWeaponInputContext;
import com.cbcfirepowercomponents.content.compact_cannon_mount.input.MountedWeaponInputStrategies;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;

/**
 * Adds an add-on weapon fallback after CBC's own mechanical-arm loading logic.
 */
@Mixin(value = CannonMountPoint.class, remap = false)
public abstract class CannonMountPointAmmoCompatMixin {
	@Inject(method = "getInsertedResultAndDoSomething", at = @At("RETURN"), cancellable = true)
	private void cbcfpc$insertAddonWeaponAmmo(ItemStack stack, boolean simulate, AbstractMountedCannonContraption cannon,
			PitchOrientedContraptionEntity entity, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack originalResult = cir.getReturnValue();
		if (!ItemStack.matches(originalResult, stack) || originalResult.getCount() != stack.getCount())
			return;
		if (MountedWeaponInputStrategies.usesNativeCannonMountLoading(cannon))
			return;

		ItemStack result = MountedWeaponInputStrategies.insert(new MountedWeaponInputContext(entity, cannon), stack, simulate);
		if (!ItemStack.matches(result, stack) || result.getCount() != stack.getCount())
			cir.setReturnValue(result);
	}
}
