package com.cbcfirepowercomponents.mixin.radar;

import com.cbcfirepowercomponents.compat.radar.RadarCompactMountCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity", remap = false)
public abstract class AutoPitchControllerBlockEntityMixin {
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lcom/happysg/radar/block/controller/pitch/AutoPitchControllerBlockEntity;resolveMount()Lcom/happysg/radar/block/controller/pitch/AutoPitchControllerBlockEntity$Mount;"
		),
		cancellable = true,
		remap = false
	)
	private void cbcfpc$tickCompactMount(CallbackInfo ci) {
		if (RadarCompactMountCompat.tickPitch((BlockEntity) (Object) this))
			ci.cancel();
	}

	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$setCompactMountTarget(Vec3 target, CallbackInfo ci) {
		if (RadarCompactMountCompat.setPitchTarget((BlockEntity) (Object) this, target))
			ci.cancel();
	}

	@Inject(method = "setAndAcquireTrack", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$setCompactTrackTarget(@Coerce Object track, @Coerce Object config, CallbackInfo ci) {
		if (RadarCompactMountCompat.setPitchTrackTarget((BlockEntity) (Object) this, track))
			ci.cancel();
	}

	@Inject(method = "setAndAcquirePos", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$setCompactBlockTarget(BlockPos pos, @Coerce Object config, boolean active, CallbackInfo ci) {
		if (RadarCompactMountCompat.setPitchBlockTarget((BlockEntity) (Object) this, pos, active))
			ci.cancel();
	}

	@Inject(method = "atTargetPitch", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$atCompactTarget(boolean strict, CallbackInfoReturnable<Boolean> cir) {
		if (RadarCompactMountCompat.hasCompactPitchMount((BlockEntity) (Object) this))
			cir.setReturnValue(RadarCompactMountCompat.atTargetPitch((BlockEntity) (Object) this));
	}
}