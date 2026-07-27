package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;
import com.simibubi.create.AllBlocks;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class CannonMountScenes {
	private CannonMountScenes() {
	}

	public static void compact(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "compact_mounts", "Using Compact Cannon Mounts");
		BlockPos mount = util.grid().at(4, 1, 4);

		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState());
		scene.overlay().showText(55)
			.text("Compact mounts hold a cannon while occupying less room than a standard mounting")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(65);

		BlockPos cannon = mount.above();
		SceneSupport.place(scene, util, cannon, facing(MTBlocks.LARGE_AUTOCANNON_BREECH.get().defaultBlockState(), Direction.UP));
		scene.overlay().showText(50)
			.text("Place the supported cannon on the mounting side")
			.pointAt(util.vector().centerOf(cannon))
			.placeNearTarget();
		scene.idle(60);

		BlockPos shaft = mount.west();
		SceneSupport.place(scene, util, shaft, AllBlocks.SHAFT.getDefaultState());
		scene.overlay().showText(55)
			.text("Supply rotational force through a shaft face to aim the cannon")
			.pointAt(util.vector().centerOf(shaft))
			.placeNearTarget();
		scene.idle(65);

		BlockPos assemblyInput = mount.north();
		BlockPos fireInput = mount.south();
		SceneSupport.place(scene, util, assemblyInput, Blocks.LEVER.defaultBlockState());
		SceneSupport.place(scene, util, fireInput, Blocks.LEVER.defaultBlockState());
		scene.overlay().showOutline(PonderPalette.GREEN, "assembly", util.select().position(assemblyInput), 65);
		scene.overlay().showOutline(PonderPalette.RED, "fire", util.select().position(fireInput), 65);
		scene.overlay().showText(65)
			.text("The opposite signal faces assemble the cannon and fire it")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(75);

		scene.world().setBlock(mount, MTBlocks.COMPACT_AUTOCANNON_MOUNT.get().defaultBlockState(), false);
		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 30)
			.withItem(new ItemStack(MTItems.COMPACT_AUTOCANNON_MOUNT.get()));
		scene.overlay().showText(45)
			.text("The autocannon variant follows the same layout but only accepts autocannons")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void vertical(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "vertical_compact_mount", "Mounting Cannons Vertically");
		BlockPos mount = util.grid().at(4, 2, 4);

		SceneSupport.place(scene, util, mount, MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get().defaultBlockState());
		scene.overlay().showText(55)
			.text("This two-support mounting places the cannon directly above or below its base")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(2.7, 2.5, 4.5),
			util.vector().of(5.3, 2.5, 4.5), 55);
		scene.overlay().showText(50)
			.text("The connecting trunnion remains horizontal between the supports")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(60);

		BlockPos upperCannon = mount.above();
		SceneSupport.place(scene, util, upperCannon,
			facing(MTBlocks.LARGE_AUTOCANNON_BREECH.get().defaultBlockState(), Direction.UP));
		scene.overlay().showText(45)
			.text("In its default orientation, the cannon is installed above the mount")
			.pointAt(util.vector().centerOf(upperCannon))
			.placeNearTarget();
		scene.idle(55);

		scene.world().destroyBlock(upperCannon);
		scene.world().modifyBlock(mount,
			state -> state.setValue(CompactCannonMountBlock.VERTICAL_DIRECTION, Direction.UP), false);
		BlockPos lowerCannon = mount.below();
		SceneSupport.place(scene, util, lowerCannon,
			facing(MTBlocks.LARGE_AUTOCANNON_BREECH.get().defaultBlockState(), Direction.DOWN));
		scene.overlay().showText(55)
			.text("Rotate the mount vertically when the cannon must hang below it")
			.pointAt(util.vector().centerOf(lowerCannon))
			.placeNearTarget();
		scene.idle(65);
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
}
