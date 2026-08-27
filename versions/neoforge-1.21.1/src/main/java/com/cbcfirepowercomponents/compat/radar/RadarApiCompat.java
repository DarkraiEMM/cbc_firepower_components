package com.cbcfirepowercomponents.compat.radar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/**
 * Optional bridge to Create Radar's public mount API introduced after 0.4.9.4.
 *
 * <p>The API has not been published as a stable Maven artifact yet, so this
 * bridge reflects only its public registry and interface contracts. No Radar
 * implementation classes are accessed. Once an official artifact containing
 * the API is released, this class can be replaced by a typed compile-only
 * integration without changing the mount behavior.</p>
 */
public final class RadarApiCompat {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String RADAR_MOD_ID = "create_radar";
	private static final String REGISTRY_CLASS = "com.happysg.radar.api.mount.RadarMountRegistry";
	private static final String PROVIDER_CLASS = "com.happysg.radar.api.mount.RadarMountProvider";
	private static final String ADAPTER_CLASS = "com.happysg.radar.api.mount.RadarMountAdapter";

	private static boolean registered;
	private static boolean apiWarned;
	private static boolean sableWarned;
	private static boolean sableProjectionInitialized;
	private static Object sableHelper;
	private static Method sableProjectOut;

	private RadarApiCompat() {
	}

	public static synchronized void register() {
		if (registered || !ModList.get().isLoaded(RADAR_MOD_ID))
			return;

		try {
			ClassLoader loader = RadarApiCompat.class.getClassLoader();
			Class<?> registryType = Class.forName(REGISTRY_CLASS, false, loader);
			Class<?> providerType = Class.forName(PROVIDER_CLASS, false, loader);
			Class<?> adapterType = Class.forName(ADAPTER_CLASS, false, loader);
			Object provider = Proxy.newProxyInstance(providerType.getClassLoader(), new Class<?>[] { providerType },
				(proxy, method, args) -> invokeProvider(proxy, method, args, adapterType));

			registryType.getMethod("register", providerType).invoke(null, provider);
			registered = true;
			LOGGER.info("Registered compact cannon mounts with Create Radar's public mount API");
		} catch (ClassNotFoundException exception) {
			LOGGER.info("Create Radar public mount API is not present; using legacy compatibility");
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnApiOnce("Create Radar's public mount API could not be registered", exception);
		}
	}

	private static Object invokeProvider(Object proxy, Method method, Object[] args,
									 Class<?> adapterType) {
		return switch (method.getName()) {
			case "find" -> findAdapter((Level) args[0], (BlockPos) args[1], adapterType);
			case "toString" -> "CBC Firepower Components radar mount provider";
			case "hashCode" -> System.identityHashCode(proxy);
			case "equals" -> proxy == args[0];
			default -> throw new UnsupportedOperationException("Unsupported Radar mount provider method: "
				+ method.getName());
		};
	}

	private static Object findAdapter(Level level, BlockPos pos, Class<?> adapterType) {
		if (!(level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount))
			return null;

		InvocationHandler handler = (proxy, method, args) -> invokeAdapter(proxy, method, args, level, pos, mount);
		return Proxy.newProxyInstance(adapterType.getClassLoader(),
			new Class<?>[] { adapterType }, handler);
	}

	private static Object invokeAdapter(Object proxy, Method method, Object[] args, Level level,
									BlockPos pos, CompactCannonMountBlockEntity mount) {
		return switch (method.getName()) {
			case "isValid" -> isValid(level, pos, mount);
			case "isAssembled" -> mount.getContraption() != null && mount.getContraption().isAlive();
			case "supportsYaw", "supportsPitch" -> true;
			case "getYaw" -> (double) mount.getCannonYaw();
			case "getPitch" -> (double) mount.getCannonPitch();
			case "setYaw" -> {
				double yaw = (double) args[0];
				if (Double.isFinite(yaw) && isValid(level, pos, mount))
					mount.setLimitedYaw((float) yaw);
				yield null;
			}
			case "setPitch" -> {
				double pitch = (double) args[0];
				if (Double.isFinite(pitch) && isValid(level, pos, mount))
					mount.setLimitedPitch((float) pitch);
				yield null;
			}
			case "getAimOrigin" -> getAimOrigin(level, mount);
			case "getMountPos" -> pos.immutable();
			case "toString" -> "CBC Firepower Components radar mount adapter at " + pos;
			case "hashCode" -> System.identityHashCode(proxy);
			case "equals" -> proxy == args[0];
			default -> throw new UnsupportedOperationException("Unsupported Radar mount adapter method: "
				+ method.getName());
		};
	}

	private static boolean isValid(Level level, BlockPos pos, CompactCannonMountBlockEntity mount) {
		return mount.getLevel() == level && !mount.isRemoved() && level.hasChunkAt(pos)
			&& level.getBlockEntity(pos) == mount;
	}

	private static Vec3 getAimOrigin(Level level, CompactCannonMountBlockEntity mount) {
		Vec3 localOrigin = mount.getInteractionLocation();
		if (!ModList.get().isLoaded("sable"))
			return localOrigin;

		initializeSableProjection();
		if (sableProjectOut == null)
			return null;

		try {
			Object projected = sableProjectOut.invoke(sableHelper, level, (Position) localOrigin);
			return projected instanceof Vec3 vec ? vec : null;
		} catch (ReflectiveOperationException | ClassCastException exception) {
			warnSableOnce("Sable could not project the radar mount origin into world space; "
				+ "radar aiming for that mount is disabled to avoid invalid coordinates", exception);
			return null;
		}
	}

	private static synchronized void initializeSableProjection() {
		if (sableProjectionInitialized)
			return;
		sableProjectionInitialized = true;

		try {
			Class<?> sableType = Class.forName("dev.ryanhcode.sable.Sable");
			Field helperField = sableType.getField("HELPER");
			sableHelper = helperField.get(null);
			sableProjectOut = sableHelper.getClass().getMethod(
				"projectOutOfSubLevel", Level.class, Position.class);
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnSableOnce("Sable is installed but its world-position projection API is incompatible; "
				+ "radar aiming is disabled instead of using sub-level coordinates", exception);
		}
	}

	private static void warnApiOnce(String message, Throwable exception) {
		if (apiWarned)
			return;
		apiWarned = true;
		LOGGER.warn(message, exception);
	}

	private static void warnSableOnce(String message, Throwable exception) {
		if (sableWarned)
			return;
		sableWarned = true;
		LOGGER.warn(message, exception);
	}
}
