package com.cbcfirepowercomponents.compat.physics;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.OptionalDouble;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

/**
 * Isolated adapter for the public Sable 2.0.x distance helper. Sable packages
 * part of the companion API as a nested JAR, so reflection is deliberately
 * limited to this adapter instead of leaking optional types into common code.
 */
final class Sable203DistanceResolver implements PhysicalDistanceResolver {
	private final Object helper;
	private final Method distanceSquaredWithSubLevels;

	Sable203DistanceResolver() throws ReflectiveOperationException {
		Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
		Field helperField = sableClass.getField("HELPER");
		this.helper = helperField.get(null);
		this.distanceSquaredWithSubLevels = this.helper.getClass().getMethod(
			"distanceSquaredWithSubLevels", Level.class, Position.class, Position.class);
	}

	@Override
	public OptionalDouble distance(Level level, Position from, Position to) {
		try {
			double squared = (double) this.distanceSquaredWithSubLevels.invoke(this.helper, level, from, to);
			return squared >= 0.0 && Double.isFinite(squared)
				? OptionalDouble.of(Math.sqrt(squared))
				: OptionalDouble.empty();
		} catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
			throw new IllegalStateException("Sable physical distance lookup failed", exception);
		}
	}
}
