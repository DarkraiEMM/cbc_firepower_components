package com.cbcfirepowercomponents.mixin.radar;

import com.cbcfirepowercomponents.compat.radar.RadarCompactMountCompat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity", remap = false)
public abstract class AutoYawControllerBlockEntityMixin {
	@Inject(method = "tick", at = @At("TAIL"), remap = false)
	private void cbcfpc$tickCompactMount(CallbackInfo ci) {
		RadarCompactMountCompat.tickYaw((BlockEntity) (Object) this);
	}

	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$setCompactMountTarget(Vec3 target, CallbackInfo ci) {
		if (RadarCompactMountCompat.setYawTarget((BlockEntity) (Object) this, target))
			ci.cancel();
	}

	@Inject(method = "atTargetYaw", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$atCompactTarget(boolean strict, CallbackInfoReturnable<Boolean> cir) {
		if (RadarCompactMountCompat.hasCompactYawMount((BlockEntity) (Object) this))
			cir.setReturnValue(RadarCompactMountCompat.atTargetYaw((BlockEntity) (Object) this));
	}
}