package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterMount;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterSettings;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

@Mixin(value = CannonMountBlockEntity.class, remap = false)
public abstract class CannonMountLimiterMixin implements CannonLimiterMount {
	@Shadow private float cannonYaw;
	@Shadow private float cannonPitch;
	@Shadow public abstract Direction getContraptionDirection();

	@Unique private ItemStack cbcfpc$limiter = ItemStack.EMPTY;
	@Unique private CannonLimiterSettings cbcfpc$settings = new CannonLimiterSettings();

	@Inject(method = "tick", at = @At(value = "INVOKE",
		target = "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountBlockEntity;applyRotation()V"))
	private void cbcfpc$clampRotation(CallbackInfo ci) {
		this.cannonPitch = cbcfpc$clampPitch(this.cannonPitch);
		this.cannonYaw = cbcfpc$clampYaw(this.cannonYaw);
	}

	@Inject(method = "write", at = @At("TAIL"))
	private void cbcfpc$writeLimiter(CompoundTag tag, boolean clientPacket,
			CallbackInfo ci) {
		if (!this.cbcfpc$limiter.isEmpty())
			tag.put("CbcfpcCannonLimiter", this.cbcfpc$limiter.save(new CompoundTag()));
	}

	@Inject(method = "read", at = @At("TAIL"))
	private void cbcfpc$readLimiter(CompoundTag tag, boolean clientPacket,
			CallbackInfo ci) {
		this.cbcfpc$limiter = tag.contains("CbcfpcCannonLimiter")
			? ItemStack.of(tag.getCompound("CbcfpcCannonLimiter")) : ItemStack.EMPTY;
		this.cbcfpc$settings = CannonLimiterSettings.get(this.cbcfpc$limiter);
	}

	@Override
	public boolean hasLimiter() {
		return !this.cbcfpc$limiter.isEmpty();
	}

	@Override
	public ItemStack getLimiterStack() {
		return this.cbcfpc$limiter;
	}

	@Override
	public void installLimiter(ItemStack stack) {
		this.cbcfpc$limiter = stack.copy();
		this.cbcfpc$limiter.setCount(1);
		this.cbcfpc$settings = CannonLimiterSettings.get(this.cbcfpc$limiter);
		this.cannonPitch = cbcfpc$clampPitch(this.cannonPitch);
		this.cannonYaw = cbcfpc$clampYaw(this.cannonYaw);
		cbcfpc$sync();
	}

	@Override
	public ItemStack removeLimiter() {
		if (this.cbcfpc$limiter.isEmpty())
			return ItemStack.EMPTY;
		ItemStack removed = this.cbcfpc$limiter.copy();
		this.cbcfpc$limiter = ItemStack.EMPTY;
		this.cbcfpc$settings = new CannonLimiterSettings();
		cbcfpc$sync();
		return removed;
	}

	@Unique
	private float cbcfpc$clampPitch(float pitch) {
		if (this.cbcfpc$limiter.isEmpty())
			return pitch;
		float result = pitch;
		if (this.cbcfpc$settings.hasPitchMin)
			result = Math.max(result, this.cbcfpc$settings.pitchMin);
		if (this.cbcfpc$settings.hasPitchMax)
			result = Math.min(result, this.cbcfpc$settings.pitchMax);
		return result;
	}

	@Unique
	private float cbcfpc$clampYaw(float yaw) {
		if (this.cbcfpc$limiter.isEmpty()
			|| !this.cbcfpc$settings.hasYawMin && !this.cbcfpc$settings.hasYawMax)
			return yaw;
		float neutralYaw = this.getContraptionDirection().toYRot();
		float delta = Mth.wrapDegrees(yaw - neutralYaw);
		if (this.cbcfpc$settings.hasYawMin)
			delta = Math.max(delta, this.cbcfpc$settings.yawMin);
		if (this.cbcfpc$settings.hasYawMax)
			delta = Math.min(delta, this.cbcfpc$settings.yawMax);
		return neutralYaw + delta;
	}

	@Unique
	private void cbcfpc$sync() {
		CannonMountBlockEntity mount = (CannonMountBlockEntity) (Object) this;
		mount.setChanged();
		if (mount.getLevel() != null)
			mount.getLevel().sendBlockUpdated(mount.getBlockPos(), mount.getBlockState(), mount.getBlockState(), 3);
	}
}
