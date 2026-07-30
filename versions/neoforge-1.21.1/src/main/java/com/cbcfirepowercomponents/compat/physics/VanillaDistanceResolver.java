package com.cbcfirepowercomponents.compat.physics;

import java.util.OptionalDouble;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

final class VanillaDistanceResolver implements PhysicalDistanceResolver {
	@Override
	public OptionalDouble distance(Level level, Position from, Position to) {
		double x = from.x() - to.x();
		double y = from.y() - to.y();
		double z = from.z() - to.z();
		double squared = x * x + y * y + z * z;
		return squared >= 0.0 && Double.isFinite(squared)
			? OptionalDouble.of(Math.sqrt(squared))
			: OptionalDouble.empty();
	}
}
