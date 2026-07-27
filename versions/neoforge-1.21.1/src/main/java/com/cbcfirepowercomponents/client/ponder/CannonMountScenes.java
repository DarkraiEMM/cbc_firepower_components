package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;

public final class CannonMountScenes {
	private CannonMountScenes() {
	}

	public static void compact(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "compact_mounts", "Using a Compact Cannon Mount");
		BlockPos mount = util.grid().at(5, 2, 4);
		BlockState mountState = MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
			.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.NORTH);
		SceneSupport.place(scene, util, mount, mountState);

		BlockPos cannonMountPoint = mount.west();
		BlockPos breech = cannonMountPoint.south();
		BlockPos chamber = cannonMountPoint;
		BlockPos barrel = cannonMountPoint.north();
		scene.overlay().showOutline(PonderPalette.GREEN, "cannon_side", util.select().position(cannonMountPoint), 45);
		scene.overlay().showText(40)
			.text("Build the cannon from this side of the mount")
			.pointAt(util.vector().centerOf(cannonMountPoint))
			.placeNearTarget();
		scene.idle(50);

		SceneSupport.place(scene, util, breech,
			facing(CBCBlocks.STEEL_QUICKFIRING_BREECH.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, chamber,
			facing(CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, barrel,
			facing(CBCBlocks.STEEL_CANNON_BARREL.getDefaultState(), Direction.NORTH));
		scene.overlay().showText(45)
			.text("Keep the breech and barrel on the same axis")
			.pointAt(util.vector().centerOf(chamber))
			.placeNearTarget();
		scene.idle(55);

		BlockPos assemblyInput = mount.south();
		BlockPos fireInput = mount.north();
		SceneSupport.place(scene, util, assemblyInput, Blocks.LEVER.defaultBlockState());
		SceneSupport.place(scene, util, fireInput, Blocks.LEVER.defaultBlockState());
		scene.overlay().showOutline(PonderPalette.GREEN, "assembly", util.select().position(assemblyInput), 45);
		scene.world().toggleRedstonePower(util.select().position(assemblyInput));
		scene.effects().indicateRedstone(assemblyInput);
		scene.overlay().showText(45)
			.text("Power this face to assemble the cannon")
			.pointAt(util.vector().centerOf(assemblyInput))
			.placeNearTarget();
		scene.idle(55);

		BlockPos yawShaft = mount.below();
		BlockPos pitchShaft = mount.east();
		SceneSupport.place(scene, util, yawShaft,
			AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
		scene.overlay().showText(45)
			.text("Drive the lower shaft to turn the mount horizontally")
			.pointAt(util.vector().centerOf(yawShaft))
			.placeNearTarget();
		scene.idle(55);

		SceneSupport.place(scene, util, pitchShaft,
			AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
		scene.overlay().showText(45)
			.text("Drive the side shaft to raise or lower the cannon")
			.pointAt(util.vector().centerOf(pitchShaft))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showOutline(PonderPalette.RED, "fire", util.select().position(fireInput), 45);
		scene.world().toggleRedstonePower(util.select().position(fireInput));
		scene.effects().indicateRedstone(fireInput);
		scene.effects().emitParticles(util.vector().blockSurface(barrel, Direction.NORTH),
			scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
				util.vector().of(0, 0, -0.12)), 1, 8);
		scene.overlay().showText(45)
			.text("Power the opposite face to fire the loaded cannon")
			.pointAt(util.vector().centerOf(fireInput))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void compactAutocannon(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "compact_autocannon_mount", "Using a Compact Autocannon Mount");
		BlockPos mount = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_AUTOCANNON_MOUNT.get().defaultBlockState());

		BlockPos cannonMountPoint = mount.above();
		BlockPos breech = cannonMountPoint.south();
		BlockPos recoilSpring = cannonMountPoint;
		BlockPos barrel = cannonMountPoint.north();
		scene.overlay().showText(40)
			.text("Build a CBC autocannon above the mount")
			.pointAt(util.vector().centerOf(breech))
			.placeNearTarget();
		scene.idle(50);

		SceneSupport.place(scene, util, breech,
			facing(CBCBlocks.STEEL_AUTOCANNON_BREECH.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, recoilSpring,
			facing(CBCBlocks.STEEL_AUTOCANNON_RECOIL_SPRING.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, barrel,
			facing(CBCBlocks.STEEL_AUTOCANNON_BARREL.getDefaultState(), Direction.NORTH));
		scene.overlay().showText(45)
			.text("Align the breech, recoil spring and barrel")
			.pointAt(util.vector().centerOf(recoilSpring))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(breech), Pointing.DOWN, 35)
			.withItem(filledAutocannonCartridge());
		scene.overlay().showText(45)
			.text("Load CBC autocannon cartridges into the breech")
			.pointAt(util.vector().centerOf(breech))
			.placeNearTarget();
		scene.idle(55);

		scene.effects().emitParticles(util.vector().blockSurface(barrel, Direction.NORTH),
			scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
				util.vector().of(0, 0, -0.12)), 1, 8);
		scene.overlay().showText(40)
			.text("Assemble, aim and fire it like the cannon mount")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(50);
		SceneSupport.finish(scene);
	}

	public static void vertical(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "vertical_compact_mount", "Using a Vertical Compact Cannon Mount");
		BlockPos mount = util.grid().at(3, 2, 4);

		SceneSupport.place(scene, util, mount, MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get().defaultBlockState());
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(2.7, 2.5, 4.5),
			util.vector().of(4.3, 2.5, 4.5), 45);
		scene.overlay().showText(40)
			.text("Keep the trunnion horizontal between the supports")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(50);

		BlockPos upperCannon = mount.above();
		SceneSupport.place(scene, util, upperCannon.south(),
			facing(CBCBlocks.STEEL_QUICKFIRING_BREECH.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, upperCannon,
			facing(CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, upperCannon.north(),
			facing(CBCBlocks.STEEL_CANNON_BARREL.getDefaultState(), Direction.NORTH));
		scene.overlay().showText(45)
			.text("Build the cannon above the mount and align it with the trunnion")
			.pointAt(util.vector().centerOf(upperCannon))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 30)
			.withItem(new ItemStack(AllItems.WRENCH.get()))
			.rightClick();
		scene.overlay().showText(40)
			.text("Use a wrench to switch the mounting side")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(50);

		scene.world().destroyBlock(upperCannon.south());
		scene.world().destroyBlock(upperCannon);
		scene.world().destroyBlock(upperCannon.north());
		scene.world().modifyBlock(mount,
			state -> state.setValue(CompactCannonMountBlock.VERTICAL_DIRECTION, Direction.UP), false);
		BlockPos lowerCannon = mount.below();
		SceneSupport.place(scene, util, lowerCannon.south(),
			facing(CBCBlocks.STEEL_QUICKFIRING_BREECH.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, lowerCannon,
			facing(CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState(), Direction.NORTH));
		SceneSupport.place(scene, util, lowerCannon.north(),
			facing(CBCBlocks.STEEL_CANNON_BARREL.getDefaultState(), Direction.NORTH));
		scene.overlay().showText(45)
			.text("The cannon can also be assembled below the mount")
			.pointAt(util.vector().centerOf(lowerCannon))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void singleLargeAutocannon(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "single_large_autocannon", "Assembling a Large Autocannon");
		BlockPos breech = util.grid().at(2, 2, 4);
		BlockPos barrel = breech.east();
		BlockPos thickBarrel = barrel.east();
		BlockPos muzzleBrake = thickBarrel.east();

		SceneSupport.place(scene, util, breech, connected(
			facing(MTBlocks.LARGE_AUTOCANNON_BREECH.get().defaultBlockState(), Direction.EAST), true, false));
		scene.overlay().showText(40)
			.text("Begin with the large autocannon breech")
			.pointAt(util.vector().centerOf(breech))
			.placeNearTarget();
		scene.idle(50);

		SceneSupport.place(scene, util, barrel, connected(
			facing(MTBlocks.STEEL_LARGE_AUTOCANNON_BARREL.get().defaultBlockState(), Direction.EAST), true, true));
		SceneSupport.place(scene, util, thickBarrel, connected(
			facing(MTBlocks.STEEL_THICK_LARGE_AUTOCANNON_BARREL.get().defaultBlockState(), Direction.EAST), true, true));
		scene.overlay().showText(55)
			.text("Continue the connected barrel in one straight line")
			.pointAt(util.vector().centerOf(thickBarrel))
			.placeNearTarget();
		scene.idle(65);

		SceneSupport.place(scene, util, muzzleBrake, connected(
			facing(MTBlocks.STEEL_LARGE_AUTOCANNON_MUZZLE_BRAKE.get().defaultBlockState(), Direction.EAST), false, true));
		scene.overlay().showText(55)
			.text("A muzzle brake reduces recoil and shortens the visible rearward travel")
			.pointAt(util.vector().centerOf(muzzleBrake))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showControls(util.vector().topOf(breech), Pointing.DOWN, 35)
			.withItem(new ItemStack(MTItems.LARGE_AUTOCANNON_ROUND.get()));
		scene.overlay().showText(50)
			.text("Feed armor-piercing or high-explosive rounds into the breech")
			.pointAt(util.vector().centerOf(breech))
			.placeNearTarget();
		scene.idle(60);

		scene.effects().emitParticles(util.vector().blockSurface(muzzleBrake, Direction.EAST),
			scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
				util.vector().of(0.12, 0, 0)), 1, 8);
		ElementLink<WorldSectionElement> recoilingBarrel =
			scene.world().makeSectionIndependent(util.select().fromTo(barrel, muzzleBrake));
		scene.world().moveSection(recoilingBarrel, new Vec3(-0.28, 0, 0), 6);
		scene.idle(8);
		scene.world().moveSection(recoilingBarrel, new Vec3(0.28, 0, 0), 12);
		scene.overlay().showText(55)
			.text("The projectile leaves from the muzzle, while the barrel performs a short recoil cycle")
			.pointAt(util.vector().blockSurface(muzzleBrake, Direction.EAST))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}

	public static void twinLargeAutocannon(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "twin_large_autocannon", "Using the Twin Large Autocannon");
		BlockPos breech = util.grid().at(2, 2, 4);
		BlockPos barrel = breech.east();
		BlockPos muzzleBrake = barrel.east();

		SceneSupport.place(scene, util, breech, connected(
			facing(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get().defaultBlockState(), Direction.EAST), true, false));
		SceneSupport.place(scene, util, barrel, connected(
			facing(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get().defaultBlockState(), Direction.EAST), true, true));
		SceneSupport.place(scene, util, muzzleBrake, connected(
			facing(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get().defaultBlockState(), Direction.EAST), false, true));
		scene.overlay().showText(55)
			.text("Twin components form two parallel firing channels")
			.pointAt(util.vector().centerOf(barrel))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showText(60)
			.text("The left and right barrels fire alternately instead of producing both shots from one tube")
			.pointAt(util.vector().blockSurface(muzzleBrake, Direction.EAST))
			.placeNearTarget();
		scene.idle(70);

		for (int i = 0; i < 4; ++i) {
			double z = i % 2 == 0 ? 4.33 : 4.67;
			scene.effects().emitParticles(util.vector().of(5.2, 2.5, z),
				scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
					util.vector().of(0.12, 0, 0)), 1, 4);
			scene.idle(12);
		}
		scene.overlay().showText(55)
			.text("Each shot starts at its own muzzle and drives only that barrel through its recoil cycle")
			.pointAt(util.vector().centerOf(muzzleBrake))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}

	private static BlockState facing(BlockState state, Direction direction) {
		return state.setValue(DirectionalBlock.FACING, direction);
	}

	private static BlockState connected(BlockState state, boolean front, boolean back) {
		return state
			.setValue(com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock.CONNECTED_FRONT, front)
			.setValue(com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock.CONNECTED_BACK, back);
	}

	private static ItemStack filledAutocannonCartridge() {
		ItemStack cartridge = new ItemStack(CBCItems.AUTOCANNON_CARTRIDGE.get());
		rbasamoyai.createbigcannons.munitions.autocannon.AutocannonCartridgeItem.writeProjectile(
			cartridge, new ItemStack(CBCItems.AP_AUTOCANNON_ROUND.get()));
		return cartridge;
	}
}
