package com.cbcfirepowercomponents.compat.radar;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;

/** Optional EA 3 public weapon bridge. No Radar types enter the mod's linkage surface. */
final class RadarPublicWeaponCompat {
	private static final String API = "com.happysg.radar.api.";
	private static boolean warned;
	private RadarPublicWeaponCompat() {}
	static Class<?> type(String name) throws ClassNotFoundException {
		return Class.forName(name, false, RadarPublicWeaponCompat.class.getClassLoader());
	}
	static boolean isAvailable() {
		try { type(API + "weapon.RadarWeaponRegistry"); return true; }
		catch (ClassNotFoundException e) { return false; }
	}
	static void register() throws ReflectiveOperationException {
		Class<?> provider = type(API + "weapon.RadarWeaponProvider");
		Object instance = Proxy.newProxyInstance(provider.getClassLoader(), new Class<?>[]{provider},
			(proxy, method, args) -> method.getName().equals("find")
				? find((ServerLevel) args[0], (BlockPos) args[1]) : objectMethod(proxy, method, args));
		type(API + "weapon.RadarWeaponRegistry").getMethod("register", provider).invoke(null, instance);
		Class<?> shot = type(API + "weapon.WeaponShotAdapter");
		Object resolver = Proxy.newProxyInstance(shot.getClassLoader(), new Class<?>[]{shot},
			(proxy, method, args) -> method.getName().equals("resolve")
				? resolveShot(args[0]) : objectMethod(proxy, method, args));
		type(API + "weapon.WeaponShotAdapterRegistry").getMethod("register", String.class, shot)
			.invoke(null, "cbc_firepower_components:public_weapon", resolver);
		LogUtils.getLogger().info("Registered compact mounts with Radar public weapon and shot APIs");
	}
	private static Object objectMethod(Object proxy, Method method, Object[] args) {
		return switch (method.getName()) {
			case "toString" -> "CBCFPC public Radar adapter";
			case "hashCode" -> System.identityHashCode(proxy);
			case "equals" -> proxy == args[0];
			default -> throw new UnsupportedOperationException(method.toString());
		};
	}
	private static Object find(ServerLevel level, BlockPos pos) throws ReflectiveOperationException {
		if (level == null || pos == null || !level.hasChunkAt(pos)
			|| !(level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)) return null;
		Class<?> adapter = type(API + "weapon.RadarWeaponAdapter");
		Object aiming = RadarApiCompat.findAdapter(level, pos, type(API + "mount.RadarMountAdapter"));
		return Proxy.newProxyInstance(adapter.getClassLoader(), new Class<?>[]{adapter}, (proxy, method, args) -> {
			boolean valid = !mount.isRemoved() && level.hasChunkAt(pos) && level.getBlockEntity(pos) == mount;
			boolean assembled = valid && mount.isRunning() && mount.getContraption() != null && mount.getContraption().isAlive();
			return switch (method.getName()) {
				case "isValid" -> valid;
				case "isAssembled" -> assembled;
				case "getMountPos" -> pos.immutable();
				case "getMount" -> aiming;
				case "getWeaponIdentity" -> mount;
				case "setFiring" -> {
					// Do not let a cached adapter modify a replacement at the same position.
					if (valid) mount.setAutomaticFirePowered(assembled && (boolean) args[0], 15);
					yield null;
				}
				default -> method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, args)
					: objectMethod(proxy, method, args);
			};
		});
	}
	private static Object resolveShot(Object context) {
		try {
			Object weapon = context.getClass().getMethod("weapon").invoke(context);
			Object identity = type(API + "weapon.RadarWeaponAdapter").getMethod("getWeaponIdentity").invoke(weapon);
			if (!(identity instanceof CompactCannonMountBlockEntity mount)) return null;
			if (!(mount.getLevel() instanceof ServerLevel level) || mount.getContraption() == null
				|| !(mount.getContraption().getContraption() instanceof AbstractMountedCannonContraption cannon)) return null;
			Vec3 origin = mount.getInteractionLocation();
			if (origin == null) return null;
			Class<?> profile = type(API + "weapon.WeaponShotProfile");
			Class<?> util = type("com.happysg.radar.compat.cbc.CannonUtil");
			Class<?> cannonType = AbstractMountedCannonContraption.class;
			boolean auto = (boolean) util.getMethod("isAutocannonFamily", cannonType).invoke(null, cannon);
			boolean big = (boolean) util.getMethod("isBigCannon", cannonType).invoke(null, cannon);
			double speed = 0;
			BallisticPropertiesComponent ballistics = null;
			int lifetime = 1200;
			if (auto) {
				ballistics = (BallisticPropertiesComponent) util.getMethod("resolveLoadedAutocannonBallistics", cannonType, Level.class).invoke(null, cannon, level);
				speed = ((Number) util.getMethod("getInitialVelocity", cannonType, ServerLevel.class).invoke(null, cannon, level)).doubleValue();
				lifetime = (int) util.getMethod("getAutocannonLifetimeTicks", cannonType, ServerLevel.class).invoke(null, cannon, level);
				BlockPos exit = cannon.getStartPos().relative(cannon.initialOrientation());
				while (cannon.presentBlockEntities.containsKey(exit)) exit = exit.relative(cannon.initialOrientation());
				Vec3 localEnd = Vec3.atCenterOf(exit.relative(cannon.initialOrientation()));
				Vec3 localCenter = Vec3.atCenterOf(BlockPos.ZERO);
				if (cannon instanceof com.cbcfirepowercomponents.content.large_autocannon.AutocannonBarrelOffset barrels) {
					localEnd = barrels.cbcfpc$offsetForCurrentTwinBarrel(localEnd);
					localCenter = barrels.cbcfpc$offsetForCurrentTwinBarrel(localCenter);
				}
				Vec3 end = mount.getContraption().toGlobalVector(localEnd, 0);
				Vec3 center = mount.getContraption().toGlobalVector(localCenter, 0);
				origin = end.subtract(end.subtract(center).normalize().scale(1.5));
			} else if (big) {
				Object state = util.getMethod("resolveBigCannonShotState", cannonType, ServerLevel.class).invoke(null, cannon, level);
				if ((boolean) state.getClass().getMethod("hasProjectile").invoke(state)) {
					speed = ((Number) state.getClass().getMethod("speed").invoke(state)).doubleValue();
					ballistics = (BallisticPropertiesComponent) state.getClass().getMethod("ballistics").invoke(state);
					BlockPos exit = (BlockPos) state.getClass().getMethod("muzzleExitLocalPos").invoke(state);
					if (exit != null) origin = mount.getContraption().toGlobalVector(Vec3.atCenterOf(exit), 0);
				}
			}
			// Never turn missing ammunition or an unknown projectile into a ready direct-fire shot.
			if (ballistics == null || !Double.isFinite(speed) || speed <= 0)
				return profile.getMethod("disabled", Vec3.class, Vec3.class, String.class, String.class)
					.invoke(null, origin, Vec3.ZERO, "cbcfpc-unavailable", "no_supported_loaded_projectile");
			Vec3 inheritedVelocity = Vec3.ZERO;
			if (net.neoforged.fml.ModList.get().isLoaded("sable")
				&& rbasamoyai.createbigcannons.config.CBCConfigs.server().compat.sableProjectilesInheritPhysicsObjectVelocity.get()) {
				Object helper = type("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
				// Sable reports blocks/second; CBC and Radar integrate once per game tick.
				inheritedVelocity = ((Vec3) helper.getClass().getMethod("getVelocity", Level.class, Vec3.class)
					.invoke(helper, level, origin)).scale(0.05);
			}
			origin = (Vec3) type(API + "physics.RadarPhysics").getMethod("worldPosition", Level.class, Vec3.class)
				.invoke(null, level, origin);
			Class<?> modelType = type(API + "weapon.ballistics.RadarProjectileModel");
			var dimension = rbasamoyai.createbigcannons.munitions.config.DimensionMunitionPropertiesHandler.getProperties(level);
			Object model = modelType.getMethod("cbc", double.class, double.class, double.class, double.class, boolean.class)
				.invoke(null, speed, ballistics.gravity() * dimension.gravityMultiplier(), ballistics.drag(), dimension.dragMultiplier(), ballistics.isQuadraticDrag());
			Class<?> preparation = type(API + "weapon.WeaponFirePreparation");
			String fingerprint = cannon.getClass().getName() + ":" + speed + ":" + ballistics + ":" + lifetime;
			return profile.getMethod("ballistic", modelType, Vec3.class, Vec3.class, int.class, String.class, boolean.class, preparation, String.class)
				.invoke(null, model, origin, inheritedVelocity, lifetime, fingerprint, true, preparation.getField("READY").get(null), "cbcfpc_loaded_projectile");
		} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
			if (!warned) { warned = true; LogUtils.getLogger().warn("Radar public weapon shot resolution failed", e); }
			return null;
		}
	}
}
