package com.cbcfirepowercomponents.content.large_autocannon;

import net.minecraft.util.Mth;

public final class TwinLargeAutocannonRecoil {
	private static final int ANIMATION_END = 5;

	private int leftAnimateTicks = ANIMATION_END;
	private int rightAnimateTicks = ANIMATION_END;
	private float leftTravelScale = 1.0f;
	private float rightTravelScale = 1.0f;

	public void handleFiring(boolean rightBarrel, boolean muzzleBrake) {
		float travelScale = muzzleBrake ? 0.55f : 1.0f;
		if (rightBarrel) {
			this.rightAnimateTicks = 0;
			this.rightTravelScale = travelScale;
		} else {
			this.leftAnimateTicks = 0;
			this.leftTravelScale = travelScale;
		}
	}

	public float getOffset(boolean rightBarrel, float partialTicks) {
		int ticks = rightBarrel ? this.rightAnimateTicks : this.leftAnimateTicks;
		float progress = (ticks + partialTicks) * 1.2f;
		if (progress <= 0.0f || progress >= 4.8f)
			return 0.0f;
		float offset = progress < 1.0f ? progress : (4.8f - progress) / 3.8f;
		float travelScale = rightBarrel ? this.rightTravelScale : this.leftTravelScale;
		return Mth.sin(offset * Mth.HALF_PI) * travelScale;
	}

	public void tick() {
		if (this.leftAnimateTicks < ANIMATION_END)
			++this.leftAnimateTicks;
		if (this.rightAnimateTicks < ANIMATION_END)
			++this.rightAnimateTicks;
	}
}
