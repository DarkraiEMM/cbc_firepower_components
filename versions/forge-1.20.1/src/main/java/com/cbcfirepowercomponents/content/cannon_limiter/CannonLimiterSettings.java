package com.cbcfirepowercomponents.content.cannon_limiter;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class CannonLimiterSettings {
	public static final float MIN_PITCH = -90.0f;
	public static final float MAX_PITCH = 90.0f;
	public static final float MIN_YAW = -180.0f;
	public static final float MAX_YAW = 180.0f;

	public boolean hasPitchMin;
	public boolean hasPitchMax;
	public boolean hasYawMin;
	public boolean hasYawMax;
	public float pitchMin;
	public float pitchMax;
	public float yawMin;
	public float yawMax;

	public static CannonLimiterSettings get(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag == null ? new CannonLimiterSettings() : fromTag(tag);
	}

	public static void save(ItemStack stack, CannonLimiterSettings settings) {
		settings.normalize();
		stack.setTag(settings.toTag());
	}

	public static CannonLimiterSettings fromTag(CompoundTag tag) {
		CannonLimiterSettings settings = new CannonLimiterSettings();
		settings.hasPitchMin = tag.contains("PitchMin");
		settings.pitchMin = settings.hasPitchMin ? tag.getFloat("PitchMin") : 0;
		settings.hasPitchMax = tag.contains("PitchMax");
		settings.pitchMax = settings.hasPitchMax ? tag.getFloat("PitchMax") : 0;
		settings.hasYawMin = tag.contains("YawMin");
		settings.yawMin = settings.hasYawMin ? tag.getFloat("YawMin") : 0;
		settings.hasYawMax = tag.contains("YawMax");
		settings.yawMax = settings.hasYawMax ? tag.getFloat("YawMax") : 0;
		return settings.normalize();
	}

	public CompoundTag toTag() {
		this.normalize();
		CompoundTag tag = new CompoundTag();
		if (this.hasPitchMin)
			tag.putFloat("PitchMin", Mth.clamp(this.pitchMin, MIN_PITCH, MAX_PITCH));
		if (this.hasPitchMax)
			tag.putFloat("PitchMax", Mth.clamp(this.pitchMax, MIN_PITCH, MAX_PITCH));
		if (this.hasYawMin)
			tag.putFloat("YawMin", Mth.clamp(this.yawMin, MIN_YAW, MAX_YAW));
		if (this.hasYawMax)
			tag.putFloat("YawMax", Mth.clamp(this.yawMax, MIN_YAW, MAX_YAW));
		return tag;
	}

	public CannonLimiterSettings normalize() {
		this.pitchMin = this.hasPitchMin ? -Mth.clamp(Math.abs(this.pitchMin), 0.0f, MAX_PITCH) : 0.0f;
		this.pitchMax = this.hasPitchMax ? Mth.clamp(Math.abs(this.pitchMax), 0.0f, MAX_PITCH) : 0.0f;
		this.yawMin = this.hasYawMin ? -Mth.clamp(Math.abs(this.yawMin), 0.0f, MAX_YAW) : 0.0f;
		this.yawMax = this.hasYawMax ? Mth.clamp(Math.abs(this.yawMax), 0.0f, MAX_YAW) : 0.0f;
		return this;
	}

	public boolean hasAnyLimit() {
		return this.hasPitchMin || this.hasPitchMax || this.hasYawMin || this.hasYawMax;
	}

	public String format(boolean enabled, float value) {
		return enabled ? String.format(Locale.ROOT, "%.1f", value) : "-";
	}
}
