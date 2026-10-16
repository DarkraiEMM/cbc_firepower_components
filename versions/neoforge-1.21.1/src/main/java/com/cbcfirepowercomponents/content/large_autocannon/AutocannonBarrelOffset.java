package com.cbcfirepowercomponents.content.large_autocannon;

import net.minecraft.world.phys.Vec3;

/** Read-only access to the same alternating barrel offset used when spawning a shot. */
public interface AutocannonBarrelOffset {
	Vec3 cbcfpc$offsetForCurrentTwinBarrel(Vec3 localPosition);
}
