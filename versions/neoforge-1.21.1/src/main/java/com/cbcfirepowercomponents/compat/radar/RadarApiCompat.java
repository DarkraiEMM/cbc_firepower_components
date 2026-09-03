package com.cbcfirepowercomponents.compat.radar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticFireMount;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;

/**
 * Production bridge to Create Radar's official public add-on mount API.
 *
 * <p>Reflection is used only to keep Create Radar an optional dependency while
 * its API is distributed with development builds rather than a Maven artifact.
 * Public mount registration uses Radar's official add-on contract. A narrowly
 * gated compatibility layer also adapts that mount to Radar's CBC firing
 * context because the public mount adapter currently covers aiming but not
 * {@code WeaponFiringControl}. Pre-API Radar releases continue to use the
 * separately gated legacy compatibility mixins.</p>
 */
public final class RadarApiCompat {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String RADAR_MOD_ID = "create_radar";
	private static final String REGISTRY_CLASS = "com.happysg.radar.api.mount.RadarMountRegistry";
	private static final String PROVIDER_CLASS = "com.happysg.radar.api.mount.RadarMountProvider";
	private static final String ADAPTER_CLASS = "com.happysg.radar.api.mount.RadarMountAdapter";
	private static final String WEAPON_REGISTRY_CLASS = "com.happysg.radar.api.weapon.WeaponShotAdapterRegistry";
	private static final String WEAPON_ADAPTER_CLASS = "com.happysg.radar.api.weapon.WeaponShotAdapter";
	private static final String WEAPON_PROFILE_CLASS = "com.happysg.radar.api.weapon.WeaponShotProfile";

	private static boolean registered;
	private static boolean apiWarned;
	private static boolean contextWarned;
	private static boolean contextActiveLogged;
	private static boolean pitchActiveLogged;
	private static boolean yawActiveLogged;
	private static boolean officialMountPathLogged;
	private static boolean structuralMountBypassLogged;
	private static boolean apiInputSpeedLogged;
	private static boolean pitchLimitFallbackLogged;
	private static boolean directAimFallbackLogged;
	private static boolean directTrackAdmissionLogged;
	private static boolean firingWarned;
	private static boolean firingActiveLogged;
	private static boolean contextInitialized;
	private static Constructor<?> contextConstructor;
	private static Constructor<?> pitchLimitsConstructor;
	private static Object compactContextKind;
	private static boolean firingBridgeInitialized;
	private static Method weaponRuntimeGet;
	private static Method weaponMountForController;
	private static Method weaponGroupFromEndpoint;
	private static Method weaponGroupMountPos;
	private static final Map<Level, Map<BlockPos, BlockPos>> ACTIVE_FIRE_LINKS = new WeakHashMap<>();
	private static boolean sableWarned;
	private static boolean sableProjectionInitialized;
	private static Object sableHelper;
	private static Method sableProjectOut;

	private RadarApiCompat() {
	}

	/** Applies the same pitch value Radar writes into a native CBC mount. */
	public static void setRadarPitch(CompactCannonMountBlockEntity mount, float pitch) {
		if (!Float.isFinite(pitch))
			return;
		mount.setLimitedPitch(pitch);
		if (!pitchActiveLogged) {
			pitchActiveLogged = true;
			LOGGER.info("Create Radar applied its first compact-mount pitch command: {} degrees", pitch);
		}
	}

	/** Applies a yaw value received through Radar's public mount adapter. */
	private static void setRadarYaw(CompactCannonMountBlockEntity mount, float yaw) {
		mount.setLimitedYaw(yaw);
		if (!yawActiveLogged) {
			yawActiveLogged = true;
			LOGGER.info("Create Radar applied its first compact-mount yaw command: {} degrees", yaw);
		}
	}

	/**
	 * CBC uses a 0/0 assembled-cannon pitch range to mean its unrestricted
	 * fallback. Radar treats the same pair as a hard zero-degree range, so retain
	 * Radar's declared limits except for that one sentinel value.
	 */
	public static Object normalizeCompactPitchLimits(Object limits) {
		if (limits == null)
			return null;
		try {
			double min = ((Number) limits.getClass().getMethod("minDegrees").invoke(limits)).doubleValue();
			double max = ((Number) limits.getClass().getMethod("maxDegrees").invoke(limits)).doubleValue();
			if (Math.abs(min) > 1.0E-5D || Math.abs(max) > 1.0E-5D)
				return limits;
			if (pitchLimitsConstructor == null) {
				Constructor<?> constructor = limits.getClass().getDeclaredConstructor(double.class, double.class);
				constructor.setAccessible(true);
				pitchLimitsConstructor = constructor;
			}
			Object normalized = pitchLimitsConstructor.newInstance(-90.0D, 90.0D);
			if (!pitchLimitFallbackLogged) {
				pitchLimitFallbackLogged = true;
				LOGGER.info("Create Radar compact-mount pitch limits normalized from CBC's unrestricted 0/0 sentinel to -90/+90 degrees");
			}
			return normalized;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's compact-mount pitch limits could not be normalized", exception);
			return limits;
		}
	}

	/**
	 * Radar resolves native CBC mounts before consulting its add-on registry. Our
	 * narrow weapon-context bridge therefore makes an API mount look native unless
	 * that one native lookup is skipped for our mount. Radar then continues through
	 * its normal resolver and selects RadarMountRegistry itself; its own
	 * ApiMountPitch/ApiMountYaw retain all control math.
	 */
	public static Object resolveCbcEndpointForMountControl(Level level, BlockPos mountPos) {
		if (level != null && mountPos != null
			&& level.getBlockEntity(mountPos) instanceof CompactCannonMountBlockEntity) {
			if (!officialMountPathLogged) {
				officialMountPathLogged = true;
				LOGGER.info("Create Radar controller deferred a compact cannon mount to its official mount API");
			}
			return null;
		}
		try {
			ClassLoader loader = RadarApiCompat.class.getClassLoader();
			Class<?> contextType = Class.forName(
				"com.happysg.radar.compat.cbc.CannonMountContext", false, loader);
			return contextType.getMethod("resolveEndpoint", Level.class, BlockPos.class)
				.invoke(null, level, mountPos);
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's native CBC mount endpoint could not be resolved", exception);
			return null;
		}
	}

	/**
	 * Radar checks Simulated's structural swivel adapter before its public mount
	 * registry. On a Simulated contraption that selection can consume the whole
	 * controller tick even when the weapon network explicitly points at our API
	 * mount. Return Radar's own "absent" result only for that mapped compact mount
	 * so normal Simulated swivel control remains untouched.
	 */
	public static Object deferStructuralKineticSelectionForOfficialMount(BlockEntity controller) {
		if (!controlsCompactApiMount(controller))
			return null;
		try {
			Class<?> resolutionType = Class.forName(
				"com.happysg.radar.block.controller.kinetic.KineticMountAdapterResolution",
				false, RadarApiCompat.class.getClassLoader());
			Object absent = resolutionType.getMethod("absent", String.class)
				.invoke(null, "cbcfpc_official_mount_api");
			if (!structuralMountBypassLogged) {
				structuralMountBypassLogged = true;
				LOGGER.info("Create Radar controller prioritized the official compact-mount API over a structural swivel candidate");
			}
			return absent;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's structural mount selection could not be deferred to its public mount API", exception);
			return null;
		}
	}

	private static boolean controlsCompactApiMount(BlockEntity controller) {
		if (controller == null || !(controller.getLevel() instanceof ServerLevel level))
			return false;

		initializeFiringBridge();
		if (weaponRuntimeGet != null && weaponMountForController != null) {
			try {
				Object runtime = weaponRuntimeGet.invoke(null, level);
				Object mapped = weaponMountForController.invoke(runtime, controller.getBlockPos());
				if (mapped instanceof BlockPos mountPos && isCompactMount(level, mountPos))
					return true;
			} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
				warnFiringOnce("Create Radar's controller mapping could not be checked for API priority", exception);
			}
		}

		BlockPos controllerPos = controller.getBlockPos();
		String controllerType = controller.getClass().getName();
		if (controllerType.endsWith("AutoYawControllerBlockEntity"))
			return isCompactMount(level, controllerPos.above()) || isCompactMount(level, controllerPos.below());
		if (controllerType.endsWith("AutoPitchControllerBlockEntity")
			&& controller.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			Direction facing = controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
			return isCompactMount(level, controllerPos.relative(facing));
		}
		return false;
	}

	private static boolean isCompactMount(Level level, BlockPos pos) {
		return pos != null && level.hasChunkAt(pos)
			&& level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity;
	}

	/**
	 * Radar 5's API handlers read the controller's generated/output speed, which
	 * is zero for the normal isolated-controller layout. Its native CBC handlers
	 * correctly read the adjacent shaft input instead. Preserve Radar's API angle
	 * math while supplying the same real input-speed source.
	 */
	public static float getOfficialMountInputSpeed(BlockEntity controller) {
		if (controller == null)
			return 0.0F;
		try {
			double speed = ((Number) controller.getClass().getMethod("getAvailableInputSpeed")
				.invoke(controller)).doubleValue();
			if (Double.isFinite(speed)) {
				if (Math.abs(speed) > 1.0E-5D && !apiInputSpeedLogged) {
					apiInputSpeedLogged = true;
					LOGGER.info("Create Radar official mount API is using adjacent shaft input: {} RPM", speed);
				}
				return (float) speed;
			}
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's public mount controller input speed could not be read", exception);
		}
		return controller instanceof KineticBlockEntity kinetic ? kinetic.getSpeed() : 0.0F;
	}

	/**
	 * Creates Radar's own cannon context for this mod's compact mount. The
	 * context mixin supplies the mount-specific operations, while Radar retains
	 * ownership of targeting, ballistics, readiness, and firing decisions.
	 */
	public static Object createCannonMountContext(BlockEntity blockEntity) {
		if (!(blockEntity instanceof CompactCannonMountBlockEntity))
			return null;
		initializeCannonMountContext();
		if (contextConstructor == null || compactContextKind == null)
			return null;
		try {
			Object context = contextConstructor.newInstance(blockEntity, compactContextKind);
			if (!contextActiveLogged) {
				contextActiveLogged = true;
				LOGGER.info("Create Radar weapon-network bridge recognized a compact cannon mount");
			}
			return context;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar could not create a cannon context for the compact mount", exception);
			return null;
		}
	}

	/**
	 * Keeps Radar's public mount adapter authoritative for movement while supplying
	 * the CBC-shaped weapon context that WeaponFiringControl still requires. This
	 * is called only from AutoPitchControllerBlockEntity#getFiringControl; exposing
	 * the context through the normal mount resolver would make Radar bypass its API
	 * pitch/yaw handlers again.
	 */
	public static Object resolveFiringControlContext(BlockEntity controller) {
		if (controller == null)
			return null;
		try {
			Method nativeResolver = controller.getClass().getDeclaredMethod("resolvePrimaryCbcMount");
			nativeResolver.setAccessible(true);
			Object nativeContext = nativeResolver.invoke(controller);
			if (nativeContext != null)
				return nativeContext;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's native firing context could not be resolved", exception);
			return null;
		}

		if (!(controller.getLevel() instanceof ServerLevel serverLevel))
			return null;
		BlockPos mountPos = resolveControllerMount(serverLevel, controller);
		if (mountPos == null || !serverLevel.hasChunkAt(mountPos))
			return null;
		return createCannonMountContext(serverLevel.getBlockEntity(mountPos));
	}

	/** Mirrors the real output of a Radar fire controller to its API mount. */
	public static void relayFireController(Level level, BlockPos fireControllerPos, boolean powered) {
		if (!(level instanceof ServerLevel serverLevel) || fireControllerPos == null)
			return;

		BlockPos resolvedMount = resolveWeaponGroupMount(serverLevel, fireControllerPos);
		boolean resolvedAutomaticMount = isAutomaticFireMount(level, resolvedMount);
		BlockPos previousMount;
		synchronized (ACTIVE_FIRE_LINKS) {
			Map<BlockPos, BlockPos> levelLinks = ACTIVE_FIRE_LINKS.computeIfAbsent(level, ignored -> new HashMap<>());
			previousMount = levelLinks.get(fireControllerPos);
			if (powered && resolvedAutomaticMount)
				levelLinks.put(fireControllerPos.immutable(), resolvedMount.immutable());
			else
				levelLinks.remove(fireControllerPos);
			if (levelLinks.isEmpty())
				ACTIVE_FIRE_LINKS.remove(level);
		}

		if (previousMount != null && (!powered || !previousMount.equals(resolvedMount)))
			setAutomaticFire(level, previousMount, false);
		if (resolvedAutomaticMount) {
			setAutomaticFire(level, resolvedMount, powered);
			if (powered && !firingActiveLogged) {
				firingActiveLogged = true;
				LOGGER.info("Create Radar firing bridge activated a compact cannon mount");
			}
		}
	}

	/**
	 * Radar's CBC pre-engagement check rejects our mount before its public weapon
	 * adapter is consulted when CBC cannot infer a positive muzzle velocity from
	 * the assembled contraption. For the direct-aim profile, retain Radar's own
	 * line-of-sight test but do not require that unrelated ballistic precheck.
	 */
	public static boolean canEngageDirectTrack(BlockEntity controller, Object track, boolean requireLineOfSight) {
		if (track == null || !(controller.getLevel() instanceof ServerLevel serverLevel))
			return false;
		BlockPos mountPos = resolveControllerMount(serverLevel, controller);
		if (mountPos == null || !serverLevel.hasChunkAt(mountPos)
			|| !(serverLevel.getBlockEntity(mountPos) instanceof CompactCannonMountBlockEntity mount)
			|| mount.getContraption() == null || !mount.getContraption().isAlive())
			return false;

		try {
			controller.getClass().getMethod("getFiringControl").invoke(controller);
			Field firingControlField = controller.getClass().getField("firingControl");
			Object firingControl = firingControlField.get(controller);
			if (firingControl == null)
				return false;
			if (requireLineOfSight) {
				Method lineOfSight = null;
				for (Method candidate : firingControl.getClass().getMethods()) {
					if (candidate.getName().equals("hasLineOfSightTo") && candidate.getParameterCount() == 2) {
						lineOfSight = candidate;
						break;
					}
				}
				if (lineOfSight == null || !Boolean.TRUE.equals(lineOfSight.invoke(firingControl, track, true)))
					return false;
			}
			if (!directTrackAdmissionLogged) {
				directTrackAdmissionLogged = true;
				LOGGER.info("Create Radar direct-aim adapter admitted a target rejected by CBC's zero-velocity ballistic precheck");
			}
			return true;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's direct-aim target admission could not be evaluated", exception);
			return false;
		}
	}

	private static synchronized void initializeCannonMountContext() {
		if (contextInitialized)
			return;
		contextInitialized = true;
		try {
			ClassLoader loader = RadarApiCompat.class.getClassLoader();
			Class<?> contextType = Class.forName("com.happysg.radar.compat.cbc.CannonMountContext", false, loader);
			Class<?> kindType = Class.forName("com.happysg.radar.compat.cbc.CannonMountContext$Kind", false, loader);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Object kind = Enum.valueOf((Class<? extends Enum>) kindType.asSubclass(Enum.class), "CBC");
			Constructor<?> constructor = contextType.getDeclaredConstructor(BlockEntity.class, kindType);
			constructor.setAccessible(true);
			contextConstructor = constructor;
			compactContextKind = kind;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnContextOnce("Create Radar's cannon context is incompatible with the compact mount bridge", exception);
		}
	}

	private static synchronized void initializeFiringBridge() {
		if (firingBridgeInitialized)
			return;
		firingBridgeInitialized = true;
		try {
			Class<?> runtimeType = Class.forName(
				"com.happysg.radar.block.behavior.networks.WeaponNetworkRuntime", false,
				RadarApiCompat.class.getClassLoader());
			try {
				weaponRuntimeGet = runtimeType.getMethod("get", ServerLevel.class);
				weaponMountForController = runtimeType.getMethod("getMountForController", BlockPos.class);
			} catch (NoSuchMethodException ignored) {
				weaponRuntimeGet = null;
				weaponMountForController = null;
			}
			try {
				weaponGroupFromEndpoint = runtimeType.getMethod(
					"getWeaponGroupViewFromEndpoint", ServerLevel.class, BlockPos.class);
			} catch (NoSuchMethodException ignored) {
				if (weaponRuntimeGet == null)
					weaponRuntimeGet = runtimeType.getMethod("get", ServerLevel.class);
				weaponGroupFromEndpoint = runtimeType.getMethod(
					"getWeaponGroupViewFromEndpoint", BlockPos.class);
			}
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnFiringOnce("Create Radar's weapon network cannot be queried for compact-mount firing", exception);
		}
	}

	/** Uses Radar's own controller resolution, followed by its network and facing fallbacks. */
	private static BlockPos resolveControllerMount(ServerLevel level, BlockEntity controller) {
		BlockPos controllerPos = controller.getBlockPos();
		try {
			Object resolved = controller.getClass().getMethod("resolveCollisionMountPositions").invoke(controller);
			if (resolved instanceof List<?> positions) {
				for (Object candidate : positions) {
					if (candidate instanceof BlockPos pos && level.hasChunkAt(pos)
						&& level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity)
						return pos;
				}
			}
		} catch (NoSuchMethodException ignored) {
			// Earlier Radar 5 API builds did not expose collision mount resolution.
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnFiringOnce("Create Radar's pitch controller could not expose its resolved mount", exception);
		}

		initializeFiringBridge();
		if (weaponRuntimeGet != null && weaponMountForController != null) {
			try {
				Object runtime = weaponRuntimeGet.invoke(null, level);
				Object mountPos = weaponMountForController.invoke(runtime, controllerPos);
				if (mountPos instanceof BlockPos pos)
					return pos;
			} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
				warnFiringOnce("Create Radar's controller mapping could not resolve its compact mount", exception);
			}
		}
		// Keeps compatibility with the short-lived API builds that exposed only
		// endpoint-to-group lookup.
		BlockPos endpointMount = resolveWeaponGroupMount(level, controllerPos);
		if (endpointMount != null)
			return endpointMount;

		// This is the final branch in Radar 5.0's own getMountPos(): an unlinked
		// controller directly operates the block it faces.
		if (controller.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			Direction facing = controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
			BlockPos adjacent = controllerPos.relative(facing);
			if (level.hasChunkAt(adjacent)
				&& level.getBlockEntity(adjacent) instanceof CompactCannonMountBlockEntity)
				return adjacent;
		}
		return null;
	}

	private static BlockPos resolveWeaponGroupMount(ServerLevel level, BlockPos fireControllerPos) {
		initializeFiringBridge();
		if (weaponGroupFromEndpoint == null)
			return null;
		try {
			Object view;
			if (Modifier.isStatic(weaponGroupFromEndpoint.getModifiers())) {
				view = weaponGroupFromEndpoint.invoke(null, level, fireControllerPos);
			} else {
				Object runtime = weaponRuntimeGet.invoke(null, level);
				view = weaponGroupFromEndpoint.invoke(runtime, fireControllerPos);
			}
			if (view == null)
				return null;
			if (weaponGroupMountPos == null || !weaponGroupMountPos.getDeclaringClass().isInstance(view))
				weaponGroupMountPos = view.getClass().getMethod("mountPos");
			Object mountPos = weaponGroupMountPos.invoke(view);
			return mountPos instanceof BlockPos pos ? pos : null;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnFiringOnce("Create Radar's weapon group could not resolve its compact mount", exception);
			return null;
		}
	}

	private static boolean isAutomaticFireMount(Level level, BlockPos pos) {
		return pos != null && level.hasChunkAt(pos)
			&& level.getBlockEntity(pos) instanceof AutomaticFireMount;
	}

	private static void setAutomaticFire(Level level, BlockPos pos, boolean powered) {
		if (pos != null && level.hasChunkAt(pos)
			&& level.getBlockEntity(pos) instanceof AutomaticFireMount mount)
			mount.setAutomaticFirePowered(powered, powered ? 15 : 0);
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
			registerWeaponShotAdapter(loader);
			registered = true;
			LOGGER.info("Registered compact cannon mounts with Create Radar's public mount API");
		} catch (ClassNotFoundException exception) {
			LOGGER.info("Create Radar public mount API is not present; using legacy compatibility");
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnApiOnce("Create Radar's public mount API could not be registered", exception);
		}
	}

	/**
	 * Registers the matching Radar 5 weapon adapter. Radar's CBC resolver remains
	 * authoritative whenever it can obtain a positive projectile velocity. The
	 * direct profile is only a fallback for an assembled weapon on our mount that
	 * Radar otherwise rejects with a zero/unknown velocity.
	 */
	private static void registerWeaponShotAdapter(ClassLoader loader) throws ReflectiveOperationException {
		Class<?> registryType = Class.forName(WEAPON_REGISTRY_CLASS, false, loader);
		Class<?> adapterType = Class.forName(WEAPON_ADAPTER_CLASS, false, loader);
		Object adapter = Proxy.newProxyInstance(adapterType.getClassLoader(), new Class<?>[] { adapterType },
			(proxy, method, args) -> switch (method.getName()) {
				case "resolve" -> resolveWeaponShotProfile(args[0], loader);
				case "toString" -> "CBC Firepower Components compact-mount weapon adapter";
				case "hashCode" -> System.identityHashCode(proxy);
				case "equals" -> proxy == args[0];
				default -> throw new UnsupportedOperationException(
					"Unsupported Radar weapon adapter method: " + method.getName());
			});
		registryType.getMethod("register", String.class, adapterType)
			.invoke(null, "cbc_firepower_components:compact_mount", adapter);
	}

	private static Object resolveWeaponShotProfile(Object shotContext, ClassLoader loader) {
		if (shotContext == null)
			return null;
		try {
			Object contextMount = shotContext.getClass().getMethod("mount").invoke(shotContext);
			if (contextMount == null)
				return null;
			Object blockEntity = contextMount.getClass().getMethod("blockEntity").invoke(contextMount);
			if (!(blockEntity instanceof CompactCannonMountBlockEntity mount))
				return null;

			Object weapon = shotContext.getClass().getMethod("weapon").invoke(shotContext);
			Object serverLevel = shotContext.getClass().getMethod("level").invoke(shotContext);
			if (weapon == null || serverLevel == null)
				return null;

			Vec3 muzzle = getAimOrigin(mount.getLevel(), mount);
			if (muzzle == null)
				return null;
			Class<?> profileType = Class.forName(WEAPON_PROFILE_CLASS, false, loader);
			Class<?> firePreparationType = Class.forName(
				"com.happysg.radar.api.weapon.WeaponFirePreparation", false, loader);
			Object ready = firePreparationType.getField("READY").get(null);
			Object profile = profileType.getMethod("direct", Vec3.class, Vec3.class, String.class,
				double.class, boolean.class, firePreparationType, String.class).invoke(null,
				muzzle, Vec3.ZERO, "cbcfpc-direct-" + weapon.getClass().getName(), 1.0D, true, ready,
				"compact_mount_unknown_projectile");
			if (!directAimFallbackLogged) {
				directAimFallbackLogged = true;
				LOGGER.info("Create Radar compact-mount weapon adapter enabled direct-aim fallback for {}",
					weapon.getClass().getName());
			}
			return profile;
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			warnApiOnce("Create Radar's weapon adapter could not resolve the compact-mount shot", exception);
			return null;
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
					setRadarYaw(mount, (float) yaw);
				yield null;
			}
			case "setPitch" -> {
				double pitch = (double) args[0];
				if (Double.isFinite(pitch) && isValid(level, pos, mount))
					setRadarPitch(mount, (float) pitch);
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

	private static void warnContextOnce(String message, Throwable exception) {
		if (contextWarned)
			return;
		contextWarned = true;
		LOGGER.warn(message, exception);
	}

	private static void warnFiringOnce(String message, Throwable exception) {
		if (firingWarned)
			return;
		firingWarned = true;
		LOGGER.warn(message, exception);
	}

	private static void warnSableOnce(String message, Throwable exception) {
		if (sableWarned)
			return;
		sableWarned = true;
		LOGGER.warn(message, exception);
	}
}
