package com.cbcfirepowercomponents.mixin.radar.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;

import net.minecraft.world.level.block.entity.BlockEntity;

/** Covers the weapon-API admission gap without replacing Radar's mount control. */
@Pseudo
@Mixin(targets = "com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity", remap = false)
public abstract class AutoPitchControllerBlockEntityMixin {
	@Redirect(method = "getFiringControl", at = @At(value = "INVOKE", target =
		"Lcom/happysg/radar/block/controller/pitch/AutoPitchControllerBlockEntity;resolvePrimaryCbcMount()Lcom/happysg/radar/compat/cbc/CannonMountContext;"), require = 1)
	@Coerce
	private Object cbcfpc$supplyWeaponContextWithoutReplacingApiMovement(@Coerce Object controller) {
		return RadarApiCompat.resolveFiringControlContext((BlockEntity) controller);
	}

	@Inject(method = "canEngageTrack", at = @At("RETURN"), cancellable = true, require = 1)
	private void cbcfpc$admitDirectCompactMountTarget(@Coerce Object track, boolean requireLineOfSight,
		CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && RadarApiCompat.canEngageDirectTrack(
			(BlockEntity) (Object) this, track, requireLineOfSight))
			cir.setReturnValue(true);
	}
}
