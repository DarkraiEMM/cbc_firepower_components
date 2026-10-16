package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** EA 3's generic weapon solver issues world-space angles, including on tilted ships. */
final class RadarWorldAim {
	private static Class<?> physics() throws ClassNotFoundException {
		return RadarPublicWeaponCompat.type("com.happysg.radar.api.physics.RadarPhysics");
	}
	static boolean moving(CompactCannonMountBlockEntity mount) throws ReflectiveOperationException {
		return RadarPublicWeaponCompat.isAvailable() && (boolean) physics()
			.getMethod("isInMovingFrame", Level.class, net.minecraft.core.BlockPos.class).invoke(null, mount.getLevel(), mount.getBlockPos());
	}
	static double[] angles(CompactCannonMountBlockEntity mount) throws ReflectiveOperationException {
		Vec3 local = direction(mount.getCannonYaw(), mount.getCannonPitch());
		Vec3 world = (Vec3) physics().getMethod("worldDirection", Vec3.class, BlockEntity.class).invoke(null, local, mount);
		return angles(world);
	}
	static double[] localAngles(CompactCannonMountBlockEntity mount, double yaw, double pitch) throws ReflectiveOperationException {
		Class<?> physics = physics();
		Vec3 origin = (Vec3) physics.getMethod("worldPosition", BlockEntity.class).invoke(null, mount);
		var inverse = physics.getMethod("localPosition", Vec3.class, BlockEntity.class);
		Vec3 start = (Vec3) inverse.invoke(null, origin, mount);
		Vec3 end = (Vec3) inverse.invoke(null, origin.add(direction(yaw, pitch)), mount);
		return angles(end.subtract(start));
	}
	private static Vec3 direction(double yaw, double pitch) {
		double y = Math.toRadians(yaw - 270), p = Math.toRadians(pitch);
		return new Vec3(Math.cos(p) * Math.cos(y), Math.sin(p), Math.cos(p) * Math.sin(y));
	}
	private static double[] angles(Vec3 direction) {
		double yaw = (Math.toDegrees(Math.atan2(direction.z, direction.x)) + 270) % 360;
		return new double[]{yaw, Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance()))};
	}
}
