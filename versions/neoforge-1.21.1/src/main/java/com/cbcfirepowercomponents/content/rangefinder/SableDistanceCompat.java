package com.cbcfirepowercomponents.content.rangefinder;

import java.util.OptionalDouble;

import com.cbcfirepowercomponents.compat.physics.PhysicalDistanceResolvers;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

/**
 * Kept as the rangefinder's small compatibility entry point so no optional
 * Sable type leaks into gameplay or network classes.
 */
final class SableDistanceCompat {
	private SableDistanceCompat() {}

	static OptionalDouble distance(Level level, Position from, Position to) {
		return PhysicalDistanceResolvers.distance(level, from, to);
	}
}
