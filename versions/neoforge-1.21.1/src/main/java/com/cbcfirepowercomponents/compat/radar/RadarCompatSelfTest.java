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
	private static final String VARIANT = System.getProperty("cbcfpc.radarTestVariant", "standard");
	private static RadarMovingSelfTest activeMoving;

	private RadarCompatSelfTest() {
	}

	public static void onServerStarted(ServerStartedEvent event) {
		if (!net.neoforged.fml.ModList.get().isLoaded("create_radar")) {
			LOGGER.info("CBCFPC_RADAR_ABSENT_TEST PASS startup=true");
			event.getServer().halt(false);
			return;
		}
		boolean passed = false;
		ServerLevel level = event.getServer().overworld();
		RadarMovingSelfTest moving = null;
		try { if (VARIANT.equals("moving")) moving = new RadarMovingSelfTest(level); }
		catch (ReflectiveOperationException e) { throw new IllegalStateException("Cannot create Sable fixture", e); }
		activeMoving = moving;
		BlockPos mountPos = moving == null ? new BlockPos(0, 200, 0) : moving.mountPos;
		BlockPos controllerPos = mountPos.east();
		BlockPos shaftPos = controllerPos.east();
		BlockPos yawControllerPos = mountPos.below();
		BlockPos yawShaftPos = yawControllerPos.below();
		boolean vertical = VARIANT.contains("vertical") || VARIANT.contains("auto-mount");
		BlockPos recoilPos = vertical ? mountPos.above() : mountPos.west();
		BlockPos breechPos = recoilPos.south();
		BlockPos barrelPos = recoilPos.north();
		try {
			Block fixtureMount = VARIANT.contains("auto-mount") ? MTBlocks.COMPACT_AUTOCANNON_MOUNT.get()
				: vertical ? MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get() : MTBlocks.COMPACT_CANNON_MOUNT.get();
			level.setBlock(mountPos, fixtureMount.defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.NORTH), 3);
			level.setBlock(breechPos, CBCBlocks.STEEL_AUTOCANNON_BREECH.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			level.setBlock(recoilPos, CBCBlocks.STEEL_AUTOCANNON_RECOIL_SPRING.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			level.setBlock(barrelPos, CBCBlocks.STEEL_AUTOCANNON_BARREL.getDefaultState()
				.setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			if (VARIANT.startsWith("large") || VARIANT.startsWith("twin")) {
				boolean twin = VARIANT.startsWith("twin");
				Block breech = twin ? MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get() : MTBlocks.LARGE_AUTOCANNON_BREECH.get();
				Block barrel = twin ? MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get() : MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get();
				level.setBlock(breechPos, breech.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
				level.setBlock(recoilPos, barrel.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
				level.setBlock(barrelPos, barrel.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
			}
			// Programmatic placement does not run CBC's item-placement callback. Build
			// the same connection graph that normal in-game placement creates before
			// asking the mount to assemble the cannon.
			if (VARIANT.equals("big")) {
				level.setBlock(breechPos, CBCBlocks.STEEL_SLIDING_BREECH.getDefaultState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
				level.setBlock(recoilPos, CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
				level.setBlock(barrelPos, CBCBlocks.STEEL_CANNON_BARREL.getDefaultState().setValue(DirectionalBlock.FACING, Direction.NORTH), 3);
				for (BlockPos part : java.util.List.of(breechPos, recoilPos, barrelPos))
					rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock.onPlace(level, part);
			} else {
				AutocannonBlock.onPlace(level, breechPos);
				AutocannonBlock.onPlace(level, recoilPos);
				AutocannonBlock.onPlace(level, barrelPos);
			}
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
				.setValue(DirectionalBlock.FACING, RadarPublicWeaponCompat.isAvailable() ? Direction.DOWN : Direction.UP), 3);
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
			var fixtureCannon = (rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption) mount.getContraption().getContraption();
			for (BlockEntity fixtureBlock : fixtureCannon.presentBlockEntities.values()) {
				if (fixtureBlock instanceof rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity breech) {
					breech.getInputBuffer().clear();
					breech.setMagazine(net.minecraft.world.item.ItemStack.EMPTY);
					breech.setOutputBuffer(net.minecraft.world.item.ItemStack.EMPTY);
				}
			}

			ClassLoader loader = RadarCompatSelfTest.class.getClassLoader();
			// setBlock does not run the queued level tick that normally calls onLoad.
			controller.onLoad();
			level.getBlockEntity(yawControllerPos).onLoad();
			if (!hasMountApi(loader)) {
				testLegacy(level, mount, controller);
				if (VARIANT.equals("legacy-fire")) testLegacyFire(level, mount, controller);
				passed = true;
				return;
			}
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
			if (context == null && !RadarPublicWeaponCompat.isAvailable())
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

			if (moving != null) moving.rotate();
			if (VARIANT.equals("big")) testBigWeapon(level, mount, controller);
			else if (RadarPublicWeaponCompat.isAvailable())
				testPublicWeapon(level, mount, controller, firingControl);
			passed = true;
			LOGGER.info("CBCFPC_RADAR_SELF_TEST PASS assembledCannon=true apiResolve=true apiPitchWrite=true publicWeaponApi={} weaponContextBridge={} firingControlRetained=true pitchLimitNormalization=true apiRouteTarget=45.0 fullPitchControllerTick=true fullYawControllerTick=true inputRpm=120.0 mountPitch={} targetPitch={} mountYaw={}",
				RadarPublicWeaponCompat.isAvailable(), context != null, mount.getCannonPitch(), targetAngle, mount.getCannonYaw());
		} catch (Throwable throwable) {
			LOGGER.error("CBCFPC_RADAR_SELF_TEST FAIL", throwable);
		} finally {
			if (level.getBlockEntity(mountPos) instanceof CompactCannonMountBlockEntity fixture)
				fixture.disassemble();
			level.setBlock(shaftPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(controllerPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(yawShaftPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(yawControllerPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(breechPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(recoilPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(barrelPos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(mountPos, Blocks.AIR.defaultBlockState(), 3);
			if (moving != null) try { moving.close(); } catch (ReflectiveOperationException e) { LOGGER.error("Sable fixture cleanup failed", e); }
			event.getServer().halt(false);
		}
		if (!passed)
			throw new IllegalStateException("Create Radar compatibility self-test failed; inspect the preceding log");
	}
	private static boolean hasMountApi(ClassLoader loader) {
		try { Class.forName("com.happysg.radar.api.mount.RadarMountRegistry", false, loader); return true; }
		catch (ClassNotFoundException e) { return false; }
	}
	private static void testLegacy(ServerLevel level, CompactCannonMountBlockEntity mount, BlockEntity controller)
		throws ReflectiveOperationException {
		BlockEntity yaw = level.getBlockEntity(mount.getBlockPos().below());
		controller.getClass().getMethod("tick").invoke(controller);
		yaw.getClass().getMethod("tick").invoke(yaw);
		((KineticBlockEntity) controller).setSpeed(120);
		((KineticBlockEntity) yaw).setSpeed(120);
		Vec3 target = mount.getBlockPos().getCenter().add(20, 10, -30);
		controller.getClass().getMethod("setTarget", Vec3.class).invoke(controller, target);
		for (int i=0; i<80; i++) {
			// Drive the test input each step; no physical generator/network is ticking.
			((KineticBlockEntity) controller).setSpeed(120);
			((KineticBlockEntity) yaw).setSpeed(120);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
		}
		float firstYaw = mount.getCannonYaw();
		controller.getClass().getMethod("setTarget", Vec3.class).invoke(controller, target.add(-40, 0, 0));
		for (int i=0; i<80; i++) {
			((KineticBlockEntity) controller).setSpeed(120);
			((KineticBlockEntity) yaw).setSpeed(120);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
		}
		if (Math.abs(mount.getCannonYaw()-firstYaw)<1 || Math.abs(mount.getCannonPitch())<1)
			throw new IllegalStateException("Legacy controller target changes did not reach mount: yawBefore=" + firstYaw
				+ " yawAfter=" + mount.getCannonYaw() + " pitch=" + mount.getCannonPitch()
				+ " pitchSpeed=" + ((KineticBlockEntity) controller).getSpeed() + " yawSpeed=" + ((KineticBlockEntity) yaw).getSpeed());
		LOGGER.info("CBCFPC_RADAR_LEGACY_TEST PASS startup=true pitch=true yaw=true targetChange=true");
	}

	private static void testPublicWeapon(ServerLevel level, CompactCannonMountBlockEntity mount,
		BlockEntity controller, Object firingControl) throws ReflectiveOperationException {
		Class<?> weaponType = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponAdapter");
		Object weapon = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponRegistry")
			.getMethod("find", ServerLevel.class, BlockPos.class).invoke(null, level, mount.getBlockPos());
		if (weapon == null || weaponType.getMethod("getWeaponIdentity").invoke(weapon) != mount)
			throw new IllegalStateException("Public weapon registry did not return our adapter");
		Class<?> controlType = firingControl.getClass();
		if ((boolean) controlType.getMethod("hasResolvableShot", weaponType, ServerLevel.class).invoke(null, weapon, level))
			throw new IllegalStateException("Empty cannon advertised a ready shot");
		var cannon = (rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption) mount.getContraption().getContraption();
		var ammo = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("createbigcannons", "ap_autocannon_round"));
		if (ammo == net.minecraft.world.item.Items.AIR) throw new IllegalStateException("Missing test ammunition");
		var cartridge = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(
			ResourceLocation.fromNamespaceAndPath("createbigcannons", "autocannon_cartridge")));
		rbasamoyai.createbigcannons.munitions.autocannon.AutocannonCartridgeItem.writeProjectile(new net.minecraft.world.item.ItemStack(ammo), cartridge);
		if (VARIANT.startsWith("large") || VARIANT.startsWith("twin")) {
			cartridge = new net.minecraft.world.item.ItemStack(VARIANT.endsWith("he")
				? com.cbcfirepowercomponents.registry.MTItems.LARGE_AUTOCANNON_HE_ROUND.get()
				: com.cbcfirepowercomponents.registry.MTItems.LARGE_AUTOCANNON_ROUND.get());
		}
		if (!cannon.insertItemIntoCannon(cartridge, false).isEmpty())
			throw new IllegalStateException("Fixture rejected ammunition");
		if (!(boolean) controlType.getMethod("hasResolvableShot", weaponType, ServerLevel.class).invoke(null, weapon, level))
			throw new IllegalStateException("Loaded cannon has no public ballistic solution");
		if (activeMoving != null) {
			Class<?> contextType = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.WeaponShotContext");
			Object context = contextType.getConstructor(ServerLevel.class, RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponContext"))
				.newInstance(level, weaponType.getMethod("createContext", ServerLevel.class).invoke(weapon, level));
			Object profile = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.WeaponShotAdapterRegistry").getMethod("resolve", contextType).invoke(null, context);
			Vec3 muzzle = (Vec3) profile.getClass().getMethod("muzzlePosition").invoke(profile);
			Vec3 inherited = (Vec3) profile.getClass().getMethod("inheritedVelocity").invoke(profile);
			Vec3 local = (Vec3) RadarPublicWeaponCompat.type("com.happysg.radar.api.physics.RadarPhysics")
				.getMethod("localPosition", Vec3.class, BlockEntity.class).invoke(null, muzzle, mount);
			Object helper = RadarPublicWeaponCompat.type("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
			Vec3 expected = ((Vec3) helper.getClass().getMethod("getVelocity", net.minecraft.world.level.Level.class, Vec3.class).invoke(helper, level, local)).scale(.05);
			boolean inherit = rbasamoyai.createbigcannons.config.CBCConfigs.server().compat.sableProjectilesInheritPhysicsObjectVelocity.get();
			if (expected.lengthSqr() < 1e-8) throw new IllegalStateException("Moving fixture has zero point velocity");
			if (inherited.distanceTo(inherit ? expected : Vec3.ZERO) > 1e-5) throw new IllegalStateException("Shot profile has wrong inherited ship velocity");
			LOGGER.info("CBCFPC_MOVING_PROFILE_TEST PASS muzzle={} inherited={} config={}", muzzle, inherited, inherit);
		}
		weaponType.getMethod("setFiring", boolean.class).invoke(weapon, true);
		if (!mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED))
			throw new IllegalStateException("Public firing command did not reach cannon");
		controlType.getMethod("stopWeaponOutput").invoke(firingControl);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED))
			throw new IllegalStateException("Radar stop did not stop cannon");
		if (VARIANT.startsWith("scan-impact")) RadarScanImpactSelfTest.run(level, mount, controller);
		else testTracking(level, mount, controller, firingControl);
		LogUtils.getLogger().info("CBCFPC_PUBLIC_WEAPON_TEST PASS registry=true emptyNotReady=true loadedBallistics=true firingSignal=true radarStop=true");
	}

	private static void testBigWeapon(ServerLevel level, CompactCannonMountBlockEntity mount, BlockEntity controller) throws ReflectiveOperationException {
		var cannon = (rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption) mount.getContraption().getContraption();
		BlockPos chargePos = cannon.getStartPos(), shotPos = chargePos.relative(cannon.initialOrientation());
		var charge = (rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity) cannon.presentBlockEntities.get(chargePos);
		var shot = (rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity) cannon.presentBlockEntities.get(shotPos);
		charge.cannonBehavior().loadBlock(new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(chargePos,
			CBCBlocks.POWDER_CHARGE.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z), null));
		shot.cannonBehavior().loadBlock(new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(shotPos,
			CBCBlocks.SOLID_SHOT.getDefaultState().setValue(DirectionalBlock.FACING, Direction.NORTH), null));
		Class<?> trackType = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.RadarTrack");
		Class<?> category = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.TrackCategory");
		Class<?> configType = RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.config.TargetingConfig");
		Object config = configType.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class)
			.newInstance(false, false, true, true, false, true, true, false, false);
		for (int x=-20; x<=20; x++) for (int z=-20; z<=20; z++) level.getChunk(x,z);
		Object track = trackType.getConstructor(String.class, Vec3.class, Vec3.class, long.class, category, String.class, float.class)
			.newInstance("cbcfpc-big", mount.getBlockPos().getCenter().add(12, 1, -20), Vec3.ZERO, level.getGameTime(), category.getField("MOB").get(null), "minecraft:pig", 1F);
		trackType.getMethod("setSynthetic", boolean.class).invoke(track, true);
		java.util.Set<java.util.UUID> existing = new java.util.HashSet<>();
		level.getAllEntities().forEach(entity -> existing.add(entity.getUUID()));
		BlockEntity yaw = level.getBlockEntity(mount.getBlockPos().below());
		for (int i=0; i<120; i++) {
			((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime()+1);
			if (i%20 == 0) controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
			Object control = controller.getClass().getField("firingControl").get(controller);
			control.getClass().getMethod("tick").invoke(control);
		}
		java.util.List<net.minecraft.world.entity.Entity> spawned = new java.util.ArrayList<>();
		level.getAllEntities().forEach(entity -> { if (!existing.contains(entity.getUUID())) spawned.add(entity); });
		boolean projectile = spawned.stream().anyMatch(entity -> entity instanceof rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile);
		spawned.forEach(net.minecraft.world.entity.Entity::discard);
		if (!projectile || !charge.cannonBehavior().block().state().isAir() || !shot.cannonBehavior().block().state().isAir())
			throw new IllegalStateException("Big cannon did not automatically consume charge and shot and create a projectile");
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Empty big cannon did not stop");
		LOGGER.info("CBCFPC_BIG_CANNON_TEST PASS automaticFire=true chargeConsumed=true shotConsumed=true projectileCreated=true emptyStop=true");
	}

	private static void testLegacyFire(ServerLevel level, CompactCannonMountBlockEntity mount, BlockEntity controller)
		throws ReflectiveOperationException {
		Class<?> trackType = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.RadarTrack");
		Class<?> categoryType = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.TrackCategory");
		Class<?> configType = RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.config.TargetingConfig");
		Object config = configType.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class,
			boolean.class, boolean.class, boolean.class, boolean.class).newInstance(false, false, true, true, false, true, true, false);
		Object track = trackType.getConstructor(String.class, Vec3.class, Vec3.class, long.class, categoryType, String.class, float.class)
			.newInstance("cbcfpc-legacy-fire", mount.getBlockPos().getCenter().add(20, 10, -30), Vec3.ZERO,
				level.getGameTime(), categoryType.getField("MOB").get(null), "minecraft:pig", 1F);
		var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
		pig.setPos(mount.getBlockPos().getCenter().add(20, 10, -30));
		pig.setNoAi(true);
		level.addFreshEntity(pig);
		track = trackType.getConstructor(net.minecraft.world.entity.Entity.class).newInstance(pig);
		BlockPos firePos = mount.getBlockPos().south();
		Block fireBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create_radar", "fire_controller"));
		if (fireBlock == Blocks.AIR) throw new IllegalStateException("Missing legacy fire controller");
		level.setBlock(firePos, fireBlock.defaultBlockState(), 3);
		Class<?> networkType = RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.WeaponNetworkData");
		Object network = networkType.getMethod("get", ServerLevel.class).invoke(null, level);
		Object group = networkType.getMethod("getOrCreateGroup", net.minecraft.resources.ResourceKey.class, BlockPos.class).invoke(network, level.dimension(), mount.getBlockPos());
		networkType.getMethod("tryMergeIntoGroup", group.getClass(), BlockPos.class, BlockPos.class, BlockPos.class)
			.invoke(network, group, mount.getBlockPos().below(), controller.getBlockPos(), firePos);
		var cannon = (rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption) mount.getContraption().getContraption();
		var cartridge = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("createbigcannons", "autocannon_cartridge")));
		rbasamoyai.createbigcannons.munitions.autocannon.AutocannonCartridgeItem.writeProjectile(
			new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("createbigcannons", "ap_autocannon_round"))), cartridge);
		if (!cannon.insertItemIntoCannon(cartridge, false).isEmpty()) throw new IllegalStateException("Legacy fixture rejected ammunition");
		BlockEntity yaw = level.getBlockEntity(mount.getBlockPos().below());
		boolean fired = false;
		for (int i = 0; i < 120; ++i) {
			((KineticBlockEntity) controller).setSpeed(120);
			((KineticBlockEntity) yaw).setSpeed(120);
			if (i % 20 == 0) controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
			RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.WeaponGroupCoordinator")
				.getMethod("onLevelTick", net.neoforged.neoforge.event.tick.LevelTickEvent.Post.class)
				.invoke(null, new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, level));
			fired |= mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED);
		}
		if (!fired) throw new IllegalStateException("Radar 4.9 discarded autoFire: aligned loaded compact mount never received firing output");
		var breech = (rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity) cannon.presentBlockEntities.get(cannon.getStartPos());
		java.util.Set<java.util.UUID> existing = new java.util.HashSet<>();
		level.getAllEntities().forEach(entity -> existing.add(entity.getUUID()));
		cannon.tick(level, mount.getContraption());
		if (!breech.getInputBuffer().isEmpty()) throw new IllegalStateException("Legacy automatic fire did not consume ammunition");
		java.util.List<net.minecraft.world.entity.Entity> spawned = new java.util.ArrayList<>();
		level.getAllEntities().forEach(entity -> { if (!existing.contains(entity.getUUID())) spawned.add(entity); });
		boolean projectile = spawned.stream().anyMatch(entity -> entity instanceof rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile);
		spawned.forEach(net.minecraft.world.entity.Entity::discard);
		if (!projectile) throw new IllegalStateException("Legacy automatic fire created no projectile");
		Object control = controller.getClass().getField("firingControl").get(controller);
		control.getClass().getMethod("tick").invoke(control);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Legacy empty cannon did not stop");
		cannon.insertItemIntoCannon(cartridge.copy(), false);
		Object disabledConfig = configType.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class,
			boolean.class, boolean.class, boolean.class, boolean.class).newInstance(false, false, true, true, false, true, false, false);
		controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, disabledConfig);
		mount.setAutomaticFirePowered(true, 15);
		control.getClass().getMethod("tick").invoke(control);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Legacy autoFire=false was ignored");
		controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
		mount.setAutomaticFirePowered(true, 15);
		pig.discard();
		control.getClass().getMethod("tick").invoke(control);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Legacy dead target did not stop");
		mount.setAutomaticFirePowered(true, 15);
		level.setBlock(firePos, Blocks.CHEST.defaultBlockState(), 3);
		controller.getClass().getMethod("tick").invoke(controller);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Broken legacy firing controller left output active");
		mount.setAutomaticFirePowered(true, 15);
		networkType.getMethod("removeController", net.minecraft.resources.ResourceKey.class, BlockPos.class).invoke(network, level.dimension(), controller.getBlockPos());
		controller.getClass().getMethod("tick").invoke(controller);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED)) throw new IllegalStateException("Legacy network unlink left output active");
		level.setBlock(firePos, Blocks.AIR.defaultBlockState(), 3);
		LOGGER.info("CBCFPC_LEGACY_FIRE_TEST PASS automaticFire=true nativeCoordinator=true ammoConsumed=true projectileCreated=true emptyStop=true deadTargetStop=true brokenControllerStop=true unlinkStop=true");
	}

	private static void testTracking(ServerLevel level, CompactCannonMountBlockEntity mount,
		BlockEntity controller, Object firingControl) throws ReflectiveOperationException {
		Class<?> trackType = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.RadarTrack");
		Class<?> categoryType = RadarPublicWeaponCompat.type("com.happysg.radar.block.radar.track.TrackCategory");
		Class<?> configType = RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.config.TargetingConfig");
		Object config = configType.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class,
			boolean.class, boolean.class, boolean.class, boolean.class, boolean.class)
			.newInstance(false, false, true, true, false, true, true, false, false);
		Vec3 worldOrigin = (Vec3) RadarPublicWeaponCompat.type("com.happysg.radar.api.physics.RadarPhysics")
			.getMethod("worldPosition", net.minecraft.world.level.Level.class, Vec3.class).invoke(null, level, mount.getBlockPos().getCenter());
		Vec3 target = worldOrigin.add(20, 8, -30);
		// The real radar scans loaded terrain. Its solver deliberately refuses
		// trajectories whose fluid/collision samples cross unloaded chunks.
		for (int x = -20; x <= 20; ++x)
			for (int z = -20; z <= 20; ++z) level.getChunk(x, z);
		Object track = trackType.getConstructor(String.class, Vec3.class, Vec3.class, long.class, categoryType, String.class, float.class)
			.newInstance("cbcfpc-test", target, Vec3.ZERO, level.getGameTime(), categoryType.getField("MOB").get(null), "minecraft:pig", 1.0F);
		trackType.getMethod("setSynthetic", boolean.class).invoke(track, true);
		controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
		BlockEntity yaw = level.getBlockEntity(mount.getBlockPos().below());
		boolean fired = false;
		float firstYaw = mount.getCannonYaw();
		for (int i = 0; i < 120; ++i) {
			((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime() + 1);
			if (activeMoving != null) activeMoving.step();
			if (i % 20 == 0) controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
			firingControl = controller.getClass().getField("firingControl").get(controller);
			firingControl.getClass().getMethod("tick").invoke(firingControl);
			fired |= mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED);
		}
		float trackedYaw = mount.getCannonYaw();
		if (Math.abs(trackedYaw - firstYaw) < 1 || Math.abs(trackedYaw - 90) < 1)
			throw new IllegalStateException("Radar target did not move the mount: " + trackedYaw);
		target = target.add(-35, 5, 0);
		trackType.getMethod("setPosition", Vec3.class).invoke(track, target);
		controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
		for (int i = 0; i < 120; ++i) {
			((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime() + 1);
			if (activeMoving != null) activeMoving.step();
			if (i % 20 == 0) controller.getClass().getMethod("setAndAcquireTrack", trackType, configType).invoke(controller, track, config);
			controller.getClass().getMethod("tick").invoke(controller);
			yaw.getClass().getMethod("tick").invoke(yaw);
			firingControl = controller.getClass().getField("firingControl").get(controller);
			firingControl.getClass().getMethod("tick").invoke(firingControl);
			fired |= mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED);
		}
		if (Math.abs(mount.getCannonYaw() - trackedYaw) < 1)
			throw new IllegalStateException("Radar did not follow target position change; before=" + trackedYaw + " after=" + mount.getCannonYaw()
				+ " targetYaw=" + yaw.getClass().getMethod("getTargetAngle").invoke(yaw));
		if (!fired) throw new IllegalStateException("Radar never automatically fired after aiming; yaw=" + mount.getCannonYaw());
		if (VARIANT.equals("moving")) {
			worldOrigin = (Vec3) RadarPublicWeaponCompat.type("com.happysg.radar.api.physics.RadarPhysics")
				.getMethod("worldPosition", net.minecraft.world.level.Level.class, Vec3.class).invoke(null, level, mount.getBlockPos().getCenter());
			double yawRad = Math.toRadians(mount.getCannonYaw() - 270), pitchRad = Math.toRadians(mount.getCannonPitch());
			Vec3 localDirection = new Vec3(Math.cos(pitchRad) * Math.cos(yawRad), Math.sin(pitchRad), Math.cos(pitchRad) * Math.sin(yawRad));
			Vec3 worldDirection = (Vec3) RadarPublicWeaponCompat.type("com.happysg.radar.api.physics.RadarPhysics")
				.getMethod("worldDirection", Vec3.class, BlockEntity.class).invoke(null, localDirection, mount);
			double error = Math.toDegrees(Math.acos(net.minecraft.util.Mth.clamp(worldDirection.normalize().dot(target.subtract(worldOrigin).normalize()), -1, 1)));
			if (error > 5) throw new IllegalStateException("Rotated ship falsely reports alignment: world aim error=" + error);
			LOGGER.info("CBCFPC_MOVING_AIM_TEST PASS rotatedShip=true worldAimError={}", error);
		}
		var cannon = (rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption) mount.getContraption().getContraption();
		var breech = (rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity) cannon.presentBlockEntities.get(cannon.getStartPos());
		var spare = breech.getInputBuffer().iterator().next().copy();
		Object shotProfile = shotProfile(level, mount);
		Vec3 predictedMuzzle = (Vec3) shotProfile.getClass().getMethod("muzzlePosition").invoke(shotProfile);
		java.util.Set<java.util.UUID> existing = new java.util.HashSet<>();
		level.getAllEntities().forEach(entity -> existing.add(entity.getUUID()));
		cannon.tick(level, mount.getContraption());
		if (!breech.getInputBuffer().isEmpty()) throw new IllegalStateException("Automatic fire did not consume the loaded cartridge");
		java.util.List<net.minecraft.world.entity.Entity> spawned = new java.util.ArrayList<>();
		level.getAllEntities().forEach(entity -> {
			if (!existing.contains(entity.getUUID())) spawned.add(entity);
		});
		boolean projectileCreated = spawned.stream().anyMatch(entity ->
			entity instanceof rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile);
		for (var entity : spawned) {
			if (entity instanceof rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile
				&& entity.position().distanceTo(predictedMuzzle) > 0.01)
				throw new IllegalStateException("Shot profile muzzle differs from spawned projectile: predicted=" + predictedMuzzle + " actual=" + entity.position());
		}
		spawned.forEach(net.minecraft.world.entity.Entity::discard);
		if (!projectileCreated) throw new IllegalStateException("Automatic fire did not create a CBC projectile");
		firingControl.getClass().getMethod("tick").invoke(firingControl);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED))
			throw new IllegalStateException("Empty cannon kept firing output enabled");
		if (!cannon.insertItemIntoCannon(spare, false).isEmpty())
			throw new IllegalStateException("Fixture rejected reload for target-loss test");
		mount.setAutomaticFirePowered(true, 15);
		firingControl.getClass().getMethod("resetTarget").invoke(firingControl);
		firingControl = controller.getClass().getField("firingControl").get(controller);
		firingControl.getClass().getMethod("tick").invoke(firingControl);
		if (mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED))
			throw new IllegalStateException("Target loss did not stop firing");
		LOGGER.info("CBCFPC_TRACKING_TEST PASS targetChange=true automaticFire=true ammoConsumed=true projectileCreated=true targetLossStop=true yawBefore={} yawAfter={}", trackedYaw, mount.getCannonYaw());
	}
	private static Object shotProfile(ServerLevel level, CompactCannonMountBlockEntity mount) throws ReflectiveOperationException {
		Class<?> weaponType = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponAdapter");
		Object weapon = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponRegistry")
			.getMethod("find", ServerLevel.class, BlockPos.class).invoke(null, level, mount.getBlockPos());
		Class<?> contextType = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.WeaponShotContext");
		Object context = contextType.getConstructor(ServerLevel.class, RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponContext"))
			.newInstance(level, weaponType.getMethod("createContext", ServerLevel.class).invoke(weapon, level));
		return RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.WeaponShotAdapterRegistry").getMethod("resolve", contextType).invoke(null, context);
	}
}
