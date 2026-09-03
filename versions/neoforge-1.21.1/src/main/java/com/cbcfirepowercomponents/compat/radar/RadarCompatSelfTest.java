package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import org.slf4j.Logger;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlock;
import rbasamoyai.createbigcannons.index.CBCBlocks;

/** A property-gated smoke test that executes against the real Radar runtime. */
public final class RadarCompatSelfTest {
	private static final Logger LOGGER = LogUtils.getLogger();

	private RadarCompatSelfTest() {
	}

	public static void onServerStarted(ServerStartedEvent event) {
		boolean passed = false;
		ServerLevel level = event.getServer().overworld();
		BlockPos mountPos = new BlockPos(0, 200, 0);
		BlockPos controllerPos = mountPos.east();
		BlockPos shaftPos = controllerPos.east();
		BlockPos yawControllerPos = mountPos.below();
		BlockPos yawShaftPos = yawControllerPos.below();
		BlockPos recoilPos = mountPos.west();
		BlockPos breechPos = recoilPos.south();
		BlockPos barrelPos = recoilPos.north();
		try {
			level.setBlock(mountPos, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.NORTH), 3);
			level.setBlock(breechPos, CBCBlocks.STEEL_AUTOCANNON_BREECH.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			level.setBlock(recoilPos, CBCBlocks.STEEL_AUTOCANNON_RECOIL_SPRING.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			level.setBlock(barrelPos, CBCBlocks.STEEL_AUTOCANNON_BARREL.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			// Programmatic placement does not run CBC's item-placement callback. Build
			// the same connection graph that normal in-game placement creates before
			// asking the mount to assemble the cannon.
			AutocannonBlock.onPlace(level, breechPos);
			AutocannonBlock.onPlace(level, recoilPos);
			AutocannonBlock.onPlace(level, barrelPos);
			Block radarController = BuiltInRegistries.BLOCK.get(
				ResourceLocation.fromNamespaceAndPath("create_radar", "auto_pitch_controller"));
			if (radarController == Blocks.AIR)
				throw new IllegalStateException("Create Radar auto pitch controller is not registered");
			level.setBlock(controllerPos, radarController.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), 3);
			level.setBlock(shaftPos, AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.X), 3);
			Block radarYawController = BuiltInRegistries.BLOCK.get(
				ResourceLocation.fromNamespaceAndPath("create_radar", "auto_yaw_controller"));
			if (radarYawController == Blocks.AIR)
				throw new IllegalStateException("Create Radar auto yaw controller is not registered");
			level.setBlock(yawControllerPos, radarYawController.defaultBlockState()
				.setValue(DirectionalBlock.FACING, Direction.UP), 3);
			level.setBlock(yawShaftPos, AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.Y), 3);

			BlockEntity rawMount = level.getBlockEntity(mountPos);
			BlockEntity controller = level.getBlockEntity(controllerPos);
			if (!(rawMount instanceof CompactCannonMountBlockEntity mount) || controller == null)
				throw new IllegalStateException("Self-test fixtures did not create their block entities");
			mount.onRedstoneUpdate(true, false, false, false, 0);
			if (!mount.isRunning() || mount.getContraption() == null || !mount.getContraption().isAlive())
				throw new IllegalStateException("Self-test cannon did not assemble on the compact mount; CBC reported "
					+ mount.getLastAssemblyException());

			ClassLoader loader = RadarCompatSelfTest.class.getClassLoader();
			Class<?> adapterType = Class.forName("com.happysg.radar.api.mount.RadarMountAdapter", true, loader);
			Class<?> registryType = Class.forName("com.happysg.radar.api.mount.RadarMountRegistry", true, loader);
			Object adapter = registryType.getMethod("find", net.minecraft.world.level.Level.class, BlockPos.class)
				.invoke(null, level, mountPos);
			if (adapter == null)
				throw new IllegalStateException("Official Radar mount API did not resolve the compact mount");
			if (!Boolean.TRUE.equals(adapter.getClass().getMethod("isAssembled").invoke(adapter)))
				throw new IllegalStateException("Official Radar mount adapter did not expose the real assembled cannon");
			adapter.getClass().getMethod("setPitch", double.class).invoke(adapter, 12.5D);
			if (Math.abs(mount.getCannonPitch() - 12.5D) > 0.001D)
				throw new IllegalStateException("Official Radar mount API did not write pitch to the compact mount");

			Class<?> contextType = Class.forName("com.happysg.radar.compat.cbc.CannonMountContext", true, loader);
			Object context = contextType.getMethod("of", BlockEntity.class).invoke(null, mount);
			if (context == null)
				throw new IllegalStateException("Radar weapon network context did not resolve the compact mount");
			Class<?> pitchLimitsType = Class.forName(
				"com.happysg.radar.compat.cbc.CannonMountContext$PitchLimits", true, loader);
			Object zeroSentinel = pitchLimitsType.getConstructor(double.class, double.class)
				.newInstance(0.0D, 0.0D);
			Object normalizedLimits = RadarApiCompat.normalizeCompactPitchLimits(zeroSentinel);
			double normalizedMin = ((Number) pitchLimitsType.getMethod("minDegrees").invoke(normalizedLimits)).doubleValue();
			double normalizedMax = ((Number) pitchLimitsType.getMethod("maxDegrees").invoke(normalizedLimits)).doubleValue();
			if (normalizedMin != -90.0D || normalizedMax != 90.0D)
				throw new IllegalStateException("CBC unrestricted pitch sentinel remained clamped to "
					+ normalizedMin + ".." + normalizedMax);
			// The weapon bridge must not steal control from Radar's public mount API.
			// ApiMountPitch.setTarget works without an assembled cannon; the CBC
			// handler does not, so this is an observable route assertion without
			// reflecting Radar's optional Clockwork-bearing cache fields.
			Vec3 origin = (Vec3) adapter.getClass().getMethod("getAimOrigin").invoke(adapter);
			controller.getClass().getMethod("setTarget", Vec3.class)
				.invoke(controller, origin.add(10.0D, 10.0D, 0.0D));
			double apiRouteTarget = ((Number) controller.getClass().getMethod("getTargetAngle")
				.invoke(controller)).doubleValue();
			if (Math.abs(apiRouteTarget - 45.0D) > 0.01D)
				throw new IllegalStateException("Radar controller did not route target calculation through ApiMountPitch; target="
					+ apiRouteTarget);

			// Exercise Radar's complete controller tick against the real assembled
			// CBC cannon. This covers structural-selection priority, mount-cache
			// refresh, API resolution and Radar's own ApiMountPitch handler.
			BlockEntity rawShaft = level.getBlockEntity(shaftPos);
			if (!(rawShaft instanceof KineticBlockEntity shaft))
				throw new IllegalStateException("Self-test fixture did not create its kinetic input shaft");
			shaft.setSpeed(120.0F);
			if (!(controller instanceof KineticBlockEntity))
				throw new IllegalStateException("Radar pitch controller is not kinetic");
			controller.getClass().getMethod("tick").invoke(controller);
			Object firingControl = controller.getClass().getField("firingControl").get(controller);
			if (firingControl == null)
				throw new IllegalStateException("Radar cleared WeaponFiringControl after selecting the public mount API");
			adapter.getClass().getMethod("setPitch", double.class).invoke(adapter, 0.0D);
			controller.getClass().getMethod("setTargetAngle", float.class).invoke(controller, 20.0F);
			controller.getClass().getMethod("tick").invoke(controller);
			double targetAngle = ((Number) controller.getClass().getMethod("getTargetAngle").invoke(controller)).doubleValue();
			if (Math.abs(targetAngle - 20.0D) > 0.01D)
				throw new IllegalStateException("Radar live target hand-off produced " + targetAngle + " degrees instead of 20.0");
			if (Math.abs(mount.getCannonPitch() - 5.0D) > 0.01D)
				throw new IllegalStateException("Radar ApiMountPitch did not advance the compact mount by 5 degrees at 120 RPM; actual="
					+ mount.getCannonPitch());

			BlockEntity yawController = level.getBlockEntity(yawControllerPos);
			if (!(yawController instanceof KineticBlockEntity))
				throw new IllegalStateException("Radar yaw controller is not kinetic");
			BlockEntity rawYawShaft = level.getBlockEntity(yawShaftPos);
			if (!(rawYawShaft instanceof KineticBlockEntity yawShaft))
				throw new IllegalStateException("Self-test fixture did not create its yaw input shaft");
			yawController.getClass().getMethod("tick").invoke(yawController);
			yawShaft.setSpeed(120.0F);
			adapter.getClass().getMethod("setYaw", double.class).invoke(adapter, 180.0D);
			yawController.getClass().getMethod("setTargetAngle", float.class).invoke(yawController, 90.0F);
			yawController.getClass().getMethod("tick").invoke(yawController);
			if (Math.abs(mount.getCannonYaw() - 175.0D) > 0.01D)
				throw new IllegalStateException("Radar ApiMountYaw did not advance the compact mount by 5 degrees at 120 RPM; actual="
					+ mount.getCannonYaw());

			passed = true;
			LOGGER.info("CBCFPC_RADAR_SELF_TEST PASS assembledCannon=true apiResolve=true apiPitchWrite=true weaponContext=true firingControlRetained=true pitchLimitSentinel=true apiRouteTarget=45.0 fullPitchControllerTick=true fullYawControllerTick=true inputRpm=120.0 mountPitch={} targetPitch={} mountYaw={}",
				mount.getCannonPitch(), targetAngle, mount.getCannonYaw());
		} catch (Throwable throwable) {
			LOGGER.error("CBCFPC_RADAR_SELF_TEST FAIL", throwable);
		} finally {
			level.setBlock(shaftPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(controllerPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(yawShaftPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(yawControllerPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(breechPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(recoilPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(barrelPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(mountPos, Blocks.AIR.defaultBlockState(), 3);
			event.getServer().halt(false);
		}
		if (!passed)
			throw new IllegalStateException("Create Radar compatibility self-test failed; inspect the preceding log");
	}
}
