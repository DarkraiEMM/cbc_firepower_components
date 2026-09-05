package com.cbcfirepowercomponents.mixin;

import com.cbcfirepowercomponents.content.large_autocannon.TwinLargeAutocannonRecoilSource;
import com.cbcfirepowercomponents.registry.MTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Mixin(MountedAutocannonContraption.class)
public abstract class MountedAutocannonContraptionTwinFireMixin {
	@Unique
	private static final String CBCFPC_NEXT_RIGHT_TAG = "CBCFPCNextTwinBarrelRight";
	@Unique
	private static final double CBCFPC_BARREL_OFFSET = 0.25d;

	@Unique
	private boolean cbcfpc$nextTwinBarrelRight;

	@ModifyArg(
		method = "fireShot",
		at = @At(
			value = "INVOKE",
			target = "Lrbasamoyai/createbigcannons/cannon_control/contraption/PitchOrientedContraptionEntity;toGlobalVector(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
			ordinal = 1
		),
		index = 0
	)
	private Vec3 cbcfpc$moveDirectionOriginToTwinBarrel(Vec3 localDirectionOrigin) {
		return this.cbcfpc$offsetForCurrentTwinBarrel(localDirectionOrigin);
	}

	@ModifyArg(
		method = "fireShot",
		at = @At(
			value = "INVOKE",
			target = "Lrbasamoyai/createbigcannons/cannon_control/contraption/PitchOrientedContraptionEntity;toGlobalVector(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
			ordinal = 2
		),
		index = 0
	)
	private Vec3 cbcfpc$moveDirectionEndToTwinBarrel(Vec3 localDirectionEnd) {
		return this.cbcfpc$offsetForCurrentTwinBarrel(localDirectionEnd);
	}

	@ModifyArg(
		method = "fireShot",
		at = @At(
			value = "INVOKE",
			target = "Lrbasamoyai/createbigcannons/cannon_control/contraption/PitchOrientedContraptionEntity;toGlobalVector(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
			ordinal = 4
		),
		index = 0
	)
	private Vec3 cbcfpc$moveShotToAlternatingTwinBarrel(Vec3 localMuzzlePosition) {
		MountedAutocannonContraption contraption = (MountedAutocannonContraption) (Object) this;
		if (!cbcfpc$isTwinLargeAutocannon(contraption))
			return localMuzzlePosition;

		Vec3 offsetMuzzlePosition = this.cbcfpc$offsetForCurrentTwinBarrel(localMuzzlePosition);
		this.cbcfpc$nextTwinBarrelRight = !this.cbcfpc$nextTwinBarrelRight;
		return offsetMuzzlePosition;
	}

	@ModifyArg(
		method = "fireShot",
		at = @At(
			value = "INVOKE",
			target = "Lrbasamoyai/createbigcannons/cannon_control/ControlPitchContraption;onRecoil(Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;)V"
		),
		index = 0
	)
	private Vec3 cbcfpc$reduceRecoilWithTwinMuzzleBrake(Vec3 recoil) {
		MountedAutocannonContraption contraption = (MountedAutocannonContraption) (Object) this;
		return cbcfpc$isLargeAutocannon(contraption) && cbcfpc$hasLargeAutocannonMuzzleBrake(contraption)
			? recoil.scale(0.55d)
			: recoil;
	}

	@Inject(method = "animate", at = @At("HEAD"))
	private void cbcfpc$animateAlternatingTwinBreech(CallbackInfo ci) {
		MountedAutocannonContraption contraption = (MountedAutocannonContraption) (Object) this;
		if (!cbcfpc$isLargeAutocannon(contraption))
			return;

		boolean twin = cbcfpc$isTwinLargeAutocannon(contraption);
		boolean rightBarrel = twin && this.cbcfpc$nextTwinBarrelRight;
		if (twin)
			this.cbcfpc$nextTwinBarrelRight = !rightBarrel;
		boolean muzzleBrake = cbcfpc$hasLargeAutocannonMuzzleBrake(contraption);
		for (var entry : contraption.presentBlockEntities.entrySet()) {
			BlockEntity present = entry.getValue();
			cbcfpc$animatePart(present, rightBarrel, muzzleBrake);
			BlockEntity clientSide = contraption.getBlockEntityClientSide(entry.getKey());
			if (clientSide != present)
				cbcfpc$animatePart(clientSide, rightBarrel, muzzleBrake);
		}
	}

	@Inject(method = "writeNBT", at = @At("RETURN"))
	private void cbcfpc$writeTwinBarrelState(boolean spawnPacket,
											CallbackInfoReturnable<CompoundTag> cir) {
		cir.getReturnValue().putBoolean(CBCFPC_NEXT_RIGHT_TAG, this.cbcfpc$nextTwinBarrelRight);
	}

	@Inject(method = "readNBT", at = @At("TAIL"))
	private void cbcfpc$readTwinBarrelState(Level level, CompoundTag tag, boolean spawnData, CallbackInfo ci) {
		this.cbcfpc$nextTwinBarrelRight = tag.getBoolean(CBCFPC_NEXT_RIGHT_TAG);
	}

	@Unique
	private static boolean cbcfpc$isTwinLargeAutocannon(MountedAutocannonContraption contraption) {
		BlockEntity breech = contraption.presentBlockEntities.get(contraption.getStartPos());
		return breech != null && breech.getBlockState().is(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get());
	}

	@Unique
	private static boolean cbcfpc$isLargeAutocannon(MountedAutocannonContraption contraption) {
		BlockEntity breech = contraption.presentBlockEntities.get(contraption.getStartPos());
		return breech != null && (breech.getBlockState().is(MTBlocks.LARGE_AUTOCANNON_BREECH.get())
			|| breech.getBlockState().is(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get()));
	}

	@Unique
	private static Direction cbcfpc$getRightDirection(Direction facing) {
		return facing.getAxis().isVertical() ? Direction.EAST : facing.getClockWise();
	}

	@Unique
	private static boolean cbcfpc$hasLargeAutocannonMuzzleBrake(MountedAutocannonContraption contraption) {
		return contraption.getBlocks().values().stream()
			.anyMatch(info -> info.state().is(MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get())
				|| info.state().is(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get()));
	}

	@Unique
	private Vec3 cbcfpc$offsetForCurrentTwinBarrel(Vec3 localPosition) {
		MountedAutocannonContraption contraption = (MountedAutocannonContraption) (Object) this;
		if (!cbcfpc$isTwinLargeAutocannon(contraption))
			return localPosition;

		Direction lateral = cbcfpc$getRightDirection(contraption.initialOrientation());
		double amount = this.cbcfpc$nextTwinBarrelRight ? CBCFPC_BARREL_OFFSET : -CBCFPC_BARREL_OFFSET;
		return localPosition.add(
			lateral.getStepX() * amount,
			lateral.getStepY() * amount,
			lateral.getStepZ() * amount
		);
	}

	@Unique
	private static void cbcfpc$animatePart(BlockEntity blockEntity, boolean rightBarrel, boolean muzzleBrake) {
		if (blockEntity instanceof TwinLargeAutocannonRecoilSource recoilSource)
			recoilSource.handleTwinFiring(rightBarrel, muzzleBrake);
	}
}
