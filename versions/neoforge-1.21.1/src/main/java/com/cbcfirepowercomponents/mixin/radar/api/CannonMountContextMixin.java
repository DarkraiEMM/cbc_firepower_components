package com.cbcfirepowercomponents.mixin.radar.api;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

/** Adapts this mod's compact mount to Radar's complete CBC weapon context. */
@Pseudo
@Mixin(targets = "com.happysg.radar.compat.cbc.CannonMountContext", remap = false)
public abstract class CannonMountContextMixin {
	@Shadow @Final private BlockEntity blockEntity;

	@Inject(method = "of", at = @At("HEAD"), cancellable = true, require = 1)
	private static void cbcfpc$createCompactContext(BlockEntity blockEntity,
		CallbackInfoReturnable<Object> cir) {
		Object context = RadarApiCompat.createCannonMountContext(blockEntity);
		if (context != null)
			cir.setReturnValue(context);
	}

	@Inject(method = "getContraption", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$getContraption(CallbackInfoReturnable<PitchOrientedContraptionEntity> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.getContraption());
	}

	@Inject(method = "setPitch", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$setPitch(float pitch, CallbackInfo ci) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount) {
			RadarApiCompat.setRadarPitch(mount, pitch);
			ci.cancel();
		}
	}

	@Inject(method = "pitchLimits", at = @At("RETURN"), cancellable = true, require = 1)
	private void cbcfpc$normalizeCompactPitchLimits(CallbackInfoReturnable<Object> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity && cir.getReturnValue() != null)
			cir.setReturnValue(RadarApiCompat.normalizeCompactPitchLimits(cir.getReturnValue()));
	}

	@Inject(method = "trySetYaw", at = @At("HEAD"), cancellable = true, require = 0)
	private void cbcfpc$setYaw(float yaw, CallbackInfoReturnable<Boolean> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount) {
			mount.setLimitedYaw(yaw);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "notifyUpdate", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$notifyUpdate(CallbackInfo ci) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount) {
			mount.notifyUpdate();
			ci.cancel();
		}
	}

	@Inject(method = "controller", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$getController(CallbackInfoReturnable<ControlPitchContraption.Block> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount);
	}

	@Inject(method = "getControllerBlockPos", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$getControllerBlockPos(CallbackInfoReturnable<BlockPos> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.getControllerBlockPos());
	}

	@Inject(method = "supportsDirectYawControl", at = @At("HEAD"), cancellable = true, require = 0)
	private void cbcfpc$supportDirectYaw(CallbackInfoReturnable<Boolean> cir) {
		if (this.blockEntity instanceof CompactCannonMountBlockEntity)
			cir.setReturnValue(true);
	}

}
