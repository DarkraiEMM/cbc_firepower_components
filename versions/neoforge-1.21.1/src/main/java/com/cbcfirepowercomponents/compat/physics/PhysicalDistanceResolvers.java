package com.cbcfirepowercomponents.compat.physics;

import java.lang.reflect.InvocationTargetException;
import java.util.OptionalDouble;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public final class PhysicalDistanceResolvers {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final PhysicalDistanceResolver VANILLA = new VanillaDistanceResolver();
	private static final String SABLE_RESOLVER =
		"com.cbcfirepowercomponents.compat.physics.Sable203DistanceResolver";

	private static boolean initialized;
	private static boolean sableInstalled;
	private static PhysicalDistanceResolver resolver = VANILLA;
	private static boolean warned;

	private PhysicalDistanceResolvers() {}

	public static OptionalDouble distance(Level level, Position from, Position to) {
		initialize();
		if (sableInstalled && resolver == null)
			return OptionalDouble.empty();
		try {
			return resolver.distance(level, from, to);
		} catch (LinkageError | RuntimeException exception) {
			warnOnce("Sable physical distance lookup failed; rangefinder measurement is unavailable", exception);
			return OptionalDouble.empty();
		}
	}

	private static synchronized void initialize() {
		if (initialized)
			return;
		initialized = true;
		sableInstalled = ModList.get().isLoaded("sable");
		if (!sableInstalled)
			return;

		try {
			Class<?> type = Class.forName(SABLE_RESOLVER);
			resolver = (PhysicalDistanceResolver) type.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
				 | IllegalAccessException | InvocationTargetException | ClassCastException
				 | LinkageError exception) {
			resolver = null;
			warnOnce("Sable is installed, but its 2.0.x distance API is incompatible; "
				+ "rangefinder measurement is disabled instead of using sub-level coordinates", exception);
		}
	}

	private static void warnOnce(String message, Throwable exception) {
		if (warned)
			return;
		warned = true;
		LOGGER.warn(message, exception);
	}
}
