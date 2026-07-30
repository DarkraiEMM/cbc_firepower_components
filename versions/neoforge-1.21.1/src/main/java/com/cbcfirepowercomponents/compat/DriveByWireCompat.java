package com.cbcfirepowercomponents.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Optional integration with Drive By Wire. Only its public network snapshot is
 * read; no DBW implementation code or persistent data is copied into this mod.
 */
public final class DriveByWireCompat {
	private static boolean initialized;
	private static Method managerGet;
	private static Method managerGetNetwork;
	private static Method sinkPosition;

	private DriveByWireCompat() {}

	public static List<BlockPos> getLinkedTargets(Level level, BlockPos source) {
		initialize();
		if (managerGet == null || managerGetNetwork == null)
			return Collections.emptyList();
		try {
			Object manager = managerGet.invoke(null, level);
			Object snapshot = managerGetNetwork.invoke(manager);
			if (!(snapshot instanceof Map<?, ?> network))
				return Collections.emptyList();
			Object sourceEntry = network.get(source.asLong());
			if (!(sourceEntry instanceof Map<?, ?> channels))
				return Collections.emptyList();

			Set<BlockPos> targets = new LinkedHashSet<>();
			for (Object channelEntry : channels.values()) {
				if (!(channelEntry instanceof Iterable<?> sinks))
					continue;
				for (Object sink : sinks) {
					Method positionMethod = getSinkPositionMethod(sink);
					if (positionMethod == null)
						continue;
					Object position = positionMethod.invoke(sink);
					if (position instanceof Long packed)
						targets.add(BlockPos.of(packed));
				}
			}
			return new ArrayList<>(targets);
		} catch (ReflectiveOperationException | LinkageError | ClassCastException | IllegalArgumentException exception) {
			return Collections.emptyList();
		}
	}

	private static synchronized void initialize() {
		if (initialized)
			return;
		initialized = true;
		try {
			Class<?> managerClass = Class.forName("edn.stratodonut.drivebywire.wire.WireNetworkManager");
			managerGet = managerClass.getMethod("get", Level.class);
			managerGetNetwork = managerClass.getMethod("getNetwork");
		} catch (ReflectiveOperationException | LinkageError | SecurityException exception) {
			managerGet = null;
			managerGetNetwork = null;
		}
	}

	private static Method getSinkPositionMethod(Object sink) {
		if (sink == null)
			return null;
		if (sinkPosition != null && sinkPosition.getDeclaringClass().isInstance(sink))
			return sinkPosition;
		try {
			sinkPosition = sink.getClass().getMethod("position");
			return sinkPosition;
		} catch (ReflectiveOperationException | LinkageError | SecurityException exception) {
			return null;
		}
	}
}
