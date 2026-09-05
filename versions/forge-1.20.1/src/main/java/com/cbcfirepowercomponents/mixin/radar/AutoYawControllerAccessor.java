package com.cbcfirepowercomponents.mixin.radar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.phys.Vec3;

@Mixin(targets = "com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity", remap = false)
public interface AutoYawControllerAccessor {
	@Invoker("setInternalTargetAngle")
	void cbcfpc$setInternalTargetAngle(double angle);

	@Invoker("setRunning")
	void cbcfpc$setRunning(boolean running);

	@Invoker("isRunningController")
	boolean cbcfpc$isRunningController();

	@Invoker("recordCbcYawWritten")
	void cbcfpc$recordCbcYawWritten(double yaw);

	@Invoker("getTargetAngle")
	double cbcfpc$getTargetAngle();

	@Invoker("isUpsideDown")
	boolean cbcfpc$isUpsideDown();

	@Invoker("computeYawToTargetDeg")
	double cbcfpc$computeYawToTargetDeg(Vec3 origin, Vec3 target);

	@Invoker("getToleranceDeg")
	static double cbcfpc$getToleranceDeg() {
		throw new AssertionError();
	}
}