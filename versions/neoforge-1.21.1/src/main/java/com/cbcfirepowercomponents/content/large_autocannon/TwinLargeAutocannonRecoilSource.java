package com.cbcfirepowercomponents.content.large_autocannon;

public interface TwinLargeAutocannonRecoilSource {
	void handleTwinFiring(boolean rightBarrel, boolean muzzleBrake);

	float getTwinAnimateOffset(boolean rightBarrel, float partialTicks);

	void incrementTwinAnimationTicks();
}
