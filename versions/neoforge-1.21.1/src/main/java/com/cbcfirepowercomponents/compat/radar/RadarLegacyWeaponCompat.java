package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

/** Keeps Radar 4.9's native firing decisions; only supplies the missing mount view. */
public final class RadarLegacyWeaponCompat {
	private static final String NETWORK = "com.happysg.radar.block.behavior.networks.";
	private static boolean warned;
	private RadarLegacyWeaponCompat() {}
	private static Object network(ServerLevel level) throws ReflectiveOperationException {
		return RadarPublicWeaponCompat.type(NETWORK + "WeaponNetworkData").getMethod("get", ServerLevel.class).invoke(null, level);
	}
	private static Object group(BlockEntity controller) throws ReflectiveOperationException {
		if (!(controller.getLevel() instanceof ServerLevel level)) return null;
		Object network = network(level);
		return network.getClass().getMethod("getWeaponGroupViewFromEndpoint", ResourceKey.class, BlockPos.class)
			.invoke(network, level.dimension(), controller.getBlockPos());
	}
	public static boolean ensureControl(BlockEntity controller) {
		try {
			var field = controller.getClass().getField("firingControl");
			Object previous = field.get(controller);
			Object group = group(controller);
			BlockPos pos = group == null ? null : (BlockPos) group.getClass().getMethod("mountPos").invoke(group);
			var level = controller.getLevel();
			if (pos == null || level == null || !level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)) {
				if (previous != null && previous.getClass().getField("cannonMount").get(previous) instanceof LegacyRadarMountView view) {
					if (!view.isRemoved()) view.owner.setAutomaticFirePowered(false, 0);
					field.set(controller, null);
				}
				return false;
			}
			if (previous != null && previous.getClass().getField("cannonMount").get(previous) instanceof LegacyRadarMountView view && view.owner == mount) {
				BlockPos firePos = (BlockPos) group.getClass().getMethod("firingPos").invoke(group);
				if (firePos == null || !level.hasChunkAt(firePos)
					|| !RadarPublicWeaponCompat.type("com.happysg.radar.block.controller.firing.FireControllerBlockEntity").isInstance(level.getBlockEntity(firePos)))
					mount.setAutomaticFirePowered(false, 0);
				return true;
			}
			if (previous != null) {
				if (previous.getClass().getField("cannonMount").get(previous) instanceof LegacyRadarMountView view && !view.isRemoved())
					view.owner.setAutomaticFirePowered(false, 0);
				previous.getClass().getMethod("resetTarget").invoke(previous);
			}
			Class<?> yawType = RadarPublicWeaponCompat.type("com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity");
			BlockPos yawPos = (BlockPos) group.getClass().getMethod("yawPos").invoke(group);
			Object yaw = yawPos != null && level.hasChunkAt(yawPos) ? level.getBlockEntity(yawPos) : null;
			if (!yawType.isInstance(yaw)) yaw = null;
			Object control = RadarPublicWeaponCompat.type(NETWORK + "WeaponFiringControl")
				.getConstructor(controller.getClass(), CannonMountBlockEntity.class, yawType)
				.newInstance(controller, new LegacyRadarMountView(mount), yaw);
			field.set(controller, control);
			control.getClass().getMethod("refreshControllers").invoke(control);
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) { warn(e); return false; }
	}
	public static boolean setTrack(BlockEntity controller, Object track, Object config) {
		if (!ensureControl(controller)) return false;
		try {
			Object control = controller.getClass().getField("firingControl").get(controller);
			if (track == null) control.getClass().getMethod("resetTarget").invoke(control);
			else {
				if (config == null) config = RadarPublicWeaponCompat.type(NETWORK + "config.TargetingConfig").getField("DEFAULT").get(null);
				Object group = group(controller);
				control.getClass().getMethod("setTarget", Vec3.class, config.getClass(), track.getClass(), group.getClass())
					.invoke(control, track.getClass().getMethod("getPosition").invoke(track), config, track, group);
			}
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) { warn(e); return false; }
	}
	public static void relay(BlockEntity controller, boolean powered) {
		try {
			Object group = group(controller);
			if (group == null) return;
			BlockPos pos = (BlockPos) group.getClass().getMethod("mountPos").invoke(group);
			if (pos != null && controller.getLevel().hasChunkAt(pos)
				&& controller.getLevel().getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)
				mount.setAutomaticFirePowered(powered && mount.isRunning() && hasAmmunition(mount), 15);
		} catch (ReflectiveOperationException | RuntimeException e) { warn(e); }
	}
	private static boolean hasAmmunition(CompactCannonMountBlockEntity mount) {
		if (mount.getContraption() == null || !mount.getContraption().isAlive()) return false;
		if (mount.getContraption().getContraption() instanceof rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption cannon
			&& cannon.presentBlockEntities.get(cannon.getStartPos()) instanceof rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity breech)
			return !breech.getInputBuffer().isEmpty()
				|| rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem.getTotalAmmoCount(breech.getMagazine()) > 0;
		return true;
	}
	private static void warn(Exception e) {
		if (!warned) { warned = true; LogUtils.getLogger().warn("Radar 4.9 compact weapon bridge failed", e); }
	}
}
