package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticFireMount;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlock;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;

/**
 * Adds a second, virtual firing input to CBC's fixed mount. The original
 * physical redstone input is retained separately and ORed with the controller
 * input so either source can keep the mount powered.
 */
@Mixin(FixedCannonMountBlockEntity.class)
public abstract class FixedCannonMountAutomaticFireMixin implements AutomaticFireMount {
	@Unique private boolean cbcfpc$physicalFirePowered;
	@Unique private int cbcfpc$physicalFirePower;
	@Unique private boolean cbcfpc$automaticFirePowered;
	@Unique private int cbcfpc$automaticFirePower;
	@Unique private boolean cbcfpc$physicalInputInitialized;
	@Unique private boolean cbcfpc$forwardingMergedSignal;

	@Inject(method = "onRedstoneUpdate", at = @At("HEAD"), cancellable = true)
	private void cbcfpc$mergeAutomaticFireSignal(boolean assemblyPowered, boolean previousAssemblyPowered,
		boolean firePowered, boolean previousFirePowered, int firePower, CallbackInfo ci) {
		if (this.cbcfpc$forwardingMergedSignal)
			return;

		this.cbcfpc$physicalInputInitialized = true;
		this.cbcfpc$physicalFirePowered = firePowered;
		this.cbcfpc$physicalFirePower = firePowered ? clampPower(firePower) : 0;

		boolean effectivePowered = firePowered || this.cbcfpc$automaticFirePowered;
		boolean previousEffectivePowered = previousFirePowered || this.cbcfpc$automaticFirePowered;
		int effectivePower = Math.max(this.cbcfpc$physicalFirePower, this.cbcfpc$automaticFirePower);
		if (effectivePowered == firePowered && previousEffectivePowered == previousFirePowered
			&& effectivePower == firePower)
			return;

		this.cbcfpc$forwardMergedSignal(assemblyPowered, previousAssemblyPowered,
			effectivePowered, previousEffectivePowered, effectivePower);
		ci.cancel();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void cbcfpc$clearStaleAutomaticSignalAfterLoad(CallbackInfo ci) {
		if (this.cbcfpc$physicalInputInitialized)
			return;
		FixedCannonMountBlockEntity mount = (FixedCannonMountBlockEntity) (Object) this;
		BlockState state = mount.getBlockState();
		if (!(state.getBlock() instanceof FixedCannonMountBlock block) || mount.getLevel() == null)
			return;

		this.cbcfpc$initializePhysicalInput(mount, state, block);
		boolean storedPowered = state.getValue(FixedCannonMountBlock.FIRE_POWERED);
		if (storedPowered != this.cbcfpc$physicalFirePowered) {
			boolean assemblyPowered = state.getValue(FixedCannonMountBlock.ASSEMBLY_POWERED);
			this.cbcfpc$forwardMergedSignal(assemblyPowered, assemblyPowered,
				this.cbcfpc$physicalFirePowered, storedPowered, this.cbcfpc$physicalFirePower);
		}
	}

	@Override
	public void setAutomaticFirePowered(boolean powered, int firePower) {
		FixedCannonMountBlockEntity mount = (FixedCannonMountBlockEntity) (Object) this;
		BlockState state = mount.getBlockState();
		if (!(state.getBlock() instanceof FixedCannonMountBlock))
			return;

		if (!this.cbcfpc$physicalInputInitialized)
			this.cbcfpc$initializePhysicalInput(mount, state, (FixedCannonMountBlock) state.getBlock());

		int clampedPower = powered ? clampPower(firePower) : 0;
		if (this.cbcfpc$automaticFirePowered == powered && this.cbcfpc$automaticFirePower == clampedPower)
			return;

		boolean previousEffectivePowered = this.cbcfpc$physicalFirePowered || this.cbcfpc$automaticFirePowered;
		this.cbcfpc$automaticFirePowered = powered;
		this.cbcfpc$automaticFirePower = clampedPower;
		boolean effectivePowered = this.cbcfpc$physicalFirePowered || powered;
		int effectivePower = Math.max(this.cbcfpc$physicalFirePower, clampedPower);
		boolean assemblyPowered = state.getValue(FixedCannonMountBlock.ASSEMBLY_POWERED);

		this.cbcfpc$forwardMergedSignal(assemblyPowered, assemblyPowered,
			effectivePowered, previousEffectivePowered, effectivePower);
	}

	@Override
	public BlockPos getAutomaticFireMountPos() {
		return ((FixedCannonMountBlockEntity) (Object) this).getBlockPos();
	}

	@Unique
	private void cbcfpc$forwardMergedSignal(boolean assemblyPowered, boolean previousAssemblyPowered,
		boolean firePowered, boolean previousFirePowered, int firePower) {
		FixedCannonMountBlockEntity mount = (FixedCannonMountBlockEntity) (Object) this;
		this.cbcfpc$forwardingMergedSignal = true;
		try {
			mount.onRedstoneUpdate(assemblyPowered, previousAssemblyPowered,
				firePowered, previousFirePowered, firePower);
		} finally {
			this.cbcfpc$forwardingMergedSignal = false;
		}
	}

	@Unique
	private void cbcfpc$initializePhysicalInput(FixedCannonMountBlockEntity mount, BlockState state,
		FixedCannonMountBlock block) {
		Level level = mount.getLevel();
		if (level == null)
			return;
		Direction firingFace = block.getFiringFace(state);
		int signal = level.getSignal(mount.getBlockPos().relative(firingFace), firingFace);
		this.cbcfpc$physicalFirePowered = signal > 0;
		this.cbcfpc$physicalFirePower = this.cbcfpc$physicalFirePowered ? clampPower(signal) : 0;
		this.cbcfpc$physicalInputInitialized = true;
	}

	@Unique
	private static int clampPower(int firePower) {
		return Math.max(1, Math.min(15, firePower));
	}
}
