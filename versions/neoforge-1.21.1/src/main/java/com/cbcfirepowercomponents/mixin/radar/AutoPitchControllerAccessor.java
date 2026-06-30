package com.cbcfirepowercomponents.mixin.radar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.phys.Vec3;

@Mixin(targets = "com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity", remap = false)
public interface AutoPitchControllerAccessor {
	@Invoker("setInternalTargetAngle")
	void cbcfpc$setInternalTargetAngle(double angle);

	@Invoker("setRunning")
	void cbcfpc$setRunning(boolean running);

	@Invoker("isRunningController")
	boolean cbcfpc$isRunningController();

	@Invoker("getTargetAngle")
	double cbcfpc$getTargetAngle();

	@Invoker("getRayStart")
	Vec3 cbcfpc$getRayStart();

	@Invoker("setLastTargetPos")
	void cbcfpc$setLastTargetPos(Vec3 target);

	@Invoker("getCbcTolerance")
	static double cbcfpc$getCbcTolerance() {
		throw new AssertionError();
	}
}