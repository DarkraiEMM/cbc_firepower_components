package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlock;
import com.cbcfirepowercomponents.registry.MTBlocks;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.index.CBCBlocks;

public final class FireControlScenes {
	private FireControlScenes() {
	}

	public static void automaticController(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "automatic_cannon_controller", "Using the Automatic Cannon Controller");
		BlockPos controller = util.grid().at(4, 1, 4);
		BlockState controllerState = MTBlocks.AUTOMATIC_CANNON_CONTROLLER.get().defaultBlockState()
			.setValue(AutomaticCannonControllerBlock.FACING, Direction.SOUTH);
		SceneSupport.place(scene, util, controller, controllerState);

		BlockPos leftMount = util.grid().at(2, 1, 3);
		BlockPos rightMount = util.grid().at(2, 1, 6);
		BlockPos leftMountPoint = leftMount.north();
		BlockPos rightMountPoint = rightMount.north();
		BlockPos leftMuzzle = leftMountPoint.east();
		BlockPos rightMuzzle = rightMountPoint.east();
		BlockState mountState = MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
			.setValue(com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock.HORIZONTAL_FACING,
				Direction.EAST);
		SceneSupport.place(scene, util, leftMount, mountState);
		SceneSupport.place(scene, util, rightMount, mountState);
		placeAutocannon(scene, util, leftMountPoint);
		placeAutocannon(scene, util, rightMountPoint);
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(controller), util.vector().centerOf(leftMount), 45);
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(controller), util.vector().centerOf(rightMount), 45);
		scene.overlay().showText(45)
			.text("Connect the controller to each cannon mount")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 25).rightClick();
		emitShot(scene, util, leftMuzzle);
		scene.overlay().showText(40)
			.text("Short right-click to send one firing command")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(50);

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 35).rightClick();
		scene.overlay().showText(45)
			.text("Hold right-click to open the mode selector")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(55);

		for (int i = 0; i < 3; ++i) {
			emitShot(scene, util, leftMuzzle);
			scene.idle(8);
		}
		scene.overlay().showText(45)
			.text("Choose single fire, three-round burst or continuous fire")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(55);

		emitShot(scene, util, leftMuzzle);
		scene.idle(8);
		emitShot(scene, util, rightMuzzle);
		scene.overlay().showText(45)
			.text("Polling fires each mount in turn")
			.pointAt(util.vector().centerOf(leftMount))
			.placeNearTarget();
		scene.idle(55);

		emitShot(scene, util, leftMuzzle);
		emitShot(scene, util, rightMuzzle);
		scene.overlay().showText(40)
			.text("Salvo fires all ready mounts together")
			.pointAt(util.vector().centerOf(rightMount))
			.placeNearTarget();
		scene.idle(50);

		scene.overlay().showText(45)
			.text("Set signal strength to control compatible autocannon fire rates")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.overlay().showText(40)
			.text("Sneak-right-click to select the next ammunition type")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(50);

		BlockPos rearWire = controller.north();
		SceneSupport.place(scene, util, rearWire, Blocks.REDSTONE_WIRE.defaultBlockState());
		scene.overlay().showOutline(PonderPalette.RED, "rear_input", util.select().position(rearWire), 45);
		scene.world().toggleRedstonePower(util.select().position(rearWire));
		scene.effects().indicateRedstone(rearWire);
		emitShot(scene, util, leftMuzzle);
		scene.overlay().showText(45)
			.text("A rising redstone edge on the rear face sends the same command")
			.pointAt(util.vector().centerOf(rearWire))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	private static void placeAutocannon(SceneBuilder scene, SceneBuildingUtil util, BlockPos mountPoint) {
		SceneSupport.place(scene, util, mountPoint.west(),
			facing(CBCBlocks.STEEL_AUTOCANNON_BREECH.getDefaultState(), Direction.EAST));
		SceneSupport.place(scene, util, mountPoint,
			facing(CBCBlocks.STEEL_AUTOCANNON_RECOIL_SPRING.getDefaultState(), Direction.EAST));
		SceneSupport.place(scene, util, mountPoint.east(),
			facing(CBCBlocks.STEEL_AUTOCANNON_BARREL.getDefaultState(), Direction.EAST));
	}

	private static void emitShot(SceneBuilder scene, SceneBuildingUtil util, BlockPos muzzle) {
		scene.effects().emitParticles(util.vector().blockSurface(muzzle, Direction.EAST),
			scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
				util.vector().of(0.12, 0, 0)), 1, 5);
	}

	private static BlockState facing(BlockState state, Direction direction) {
		return state.setValue(DirectionalBlock.FACING, direction);
	}
}
