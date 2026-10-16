package com.cbcfirepowercomponents.compat.radar;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import org.joml.Vector3d;
import org.joml.Quaterniond;

/** Real Sable sub-level fixture, loaded only by the opt-in developer self-test. */
final class RadarMovingSelfTest implements AutoCloseable {
	private final Object container, ship, pose;
	private Object handle;
	final BlockPos mountPos;
	RadarMovingSelfTest(ServerLevel level) throws ReflectiveOperationException {
		Class<?> containers = type("api.sublevel.SubLevelContainer");
		container = call(containers, null, "getContainer", type("api.sublevel.ServerSubLevelContainer"), new Class<?>[]{ServerLevel.class}, level);
		Class<?> poses = type("companion.math.Pose3d");
		pose = poses.getConstructor().newInstance();
		ship = call(containers, container, "allocateNewSubLevel", type("sublevel.SubLevel"), new Class<?>[]{poses}, pose);
		Object plot = type("sublevel.SubLevel").getMethod("getPlot").invoke(ship);
		mountPos = ((BlockPos) plot.getClass().getMethod("getCenterBlock").invoke(plot)).atY(200);
		Object actualPose = type("sublevel.SubLevel").getMethod("logicalPose").invoke(ship);
		((Vector3d) poses.getMethod("rotationPoint").invoke(actualPose)).set(mountPos.getX() + .5, 200.5, mountPos.getZ() + .5);
		((Vector3d) poses.getMethod("position").invoke(actualPose)).set(.5, 200.5, .5);
		for (int x=-1; x<=1; x++) for (int z=-1; z<=1; z++)
			plot.getClass().getMethod("newEmptyChunk", ChunkPos.class).invoke(plot, new ChunkPos((mountPos.getX() >> 4)+x, (mountPos.getZ() >> 4)+z));
	}
	void rotate() throws ReflectiveOperationException {
		Object actualPose = type("sublevel.SubLevel").getMethod("logicalPose").invoke(ship);
		((Quaterniond) actualPose.getClass().getMethod("orientation").invoke(actualPose)).rotationYXZ(Math.toRadians(35), Math.toRadians(12), Math.toRadians(-8));
		((Vector3d) actualPose.getClass().getMethod("position").invoke(actualPose)).set(.5, 200.5, .5);
		((Vector3d) actualPose.getClass().getMethod("rotationPoint").invoke(actualPose)).set(mountPos.getX() + .5, 200.5, mountPos.getZ() + .5);
		type("sublevel.SubLevel").getMethod("updateLastPose").invoke(ship);
		type("sublevel.SubLevel").getMethod("forceUpdateGlobalBounds").invoke(ship);
		com.mojang.logging.LogUtils.getLogger().info("CBCFPC_MOVING_FIXTURE pose={} mount={}", actualPose, mountPos);
		handle = type("api.physics.handle.RigidBodyHandle").getMethod("of", type("sublevel.ServerSubLevel")).invoke(null, ship);
		if (!(boolean) handle.getClass().getMethod("isValid").invoke(handle)) throw new IllegalStateException("Sable fixture has no physics body");
		syncBody(actualPose);
		handle.getClass().getMethod("addLinearAndAngularVelocity", org.joml.Vector3dc.class, org.joml.Vector3dc.class)
			.invoke(handle, new Vector3d(.5, 0, .2), new Vector3d(0, .01, 0));
	}
	void step() throws ReflectiveOperationException {
		Object actualPose = type("sublevel.SubLevel").getMethod("logicalPose").invoke(ship);
		type("sublevel.SubLevel").getMethod("updateLastPose").invoke(ship);
		((Vector3d) actualPose.getClass().getMethod("position").invoke(actualPose)).add(.025, 0, .01);
		((Quaterniond) actualPose.getClass().getMethod("orientation").invoke(actualPose)).rotateLocalY(.0005);
		syncBody(actualPose);
		type("sublevel.SubLevel").getMethod("forceUpdateGlobalBounds").invoke(ship);
	}
	private void syncBody(Object actualPose) throws ReflectiveOperationException {
		handle.getClass().getMethod("teleport", org.joml.Vector3dc.class, org.joml.Quaterniondc.class).invoke(handle,
			actualPose.getClass().getMethod("position").invoke(actualPose), actualPose.getClass().getMethod("orientation").invoke(actualPose));
	}
	private static Class<?> type(String name) throws ClassNotFoundException { return Class.forName("dev.ryanhcode.sable." + name); }
	private static Object call(Class<?> owner, Object receiver, String name, Class<?> result, Class<?>[] parameters, Object... args) throws ReflectiveOperationException {
		try {
			var signature = java.lang.invoke.MethodType.methodType(result, parameters);
			var lookup = java.lang.invoke.MethodHandles.publicLookup();
			var handle = receiver == null ? lookup.findStatic(owner, name, signature) : lookup.findVirtual(owner, name, signature).bindTo(receiver);
			return handle.invokeWithArguments(args);
		} catch (Throwable e) { throw new ReflectiveOperationException(name, e); }
	}
	@Override public void close() throws ReflectiveOperationException {
		Class<?> reason = type("sublevel.storage.SubLevelRemovalReason");
		call(type("api.sublevel.SubLevelContainer"), container, "removeSubLevel", void.class, new Class<?>[]{type("sublevel.SubLevel"), reason}, ship, reason.getField("REMOVED").get(null));
		call(type("api.sublevel.SubLevelContainer"), container, "processSubLevelRemovals", void.class, new Class<?>[]{ });
	}
}
