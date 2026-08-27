package com.cbcfirepowercomponents.mixin.vestalihy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "com.vestalihy.compat.CBCHelper", remap = false)
public abstract class CBCHelperMixin {
	@Inject(method = "isCannonMount", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$isCannonMount(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity)
			cir.setReturnValue(true);
	}

	@Inject(method = "isCompactCannonMount", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$isCompactCannonMount(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity)
			cir.setReturnValue(true);
	}

	@Inject(method = "getCompactContraption", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$getCompactContraption(BlockEntity blockEntity, CallbackInfoReturnable<Entity> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.getContraption());
	}

	@Inject(method = "isRunning", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$isRunning(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.isRunning());
	}

	@Inject(method = "getYawOffset", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$getYawOffset(BlockEntity blockEntity, float partialTicks,
			CallbackInfoReturnable<Float> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.getContraption() == null ? mount.getYawOffset(partialTicks)
				: mount.getContraption().getViewYRot(partialTicks));
	}

	@Inject(method = "getPitchOffset", at = @At("HEAD"), cancellable = true)
	private static void cbcfpc$getPitchOffset(BlockEntity blockEntity, float partialTicks,
			CallbackInfoReturnable<Float> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity mount)
			cir.setReturnValue(mount.getPitchOffset(partialTicks));
	}
}
