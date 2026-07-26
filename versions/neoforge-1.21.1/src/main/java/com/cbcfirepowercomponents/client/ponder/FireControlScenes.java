package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTBlocks;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

public final class FireControlScenes {
	private FireControlScenes() {
	}

	public static void automaticController(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "automatic_cannon_controller", "Controlling Multiple Cannons");
		BlockPos controller = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, controller, MTBlocks.AUTOMATIC_CANNON_CONTROLLER.get().defaultBlockState());

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 25).rightClick();
		scene.overlay().showText(45)
			.text("A short right-click sends one firing command")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 35).rightClick();
		scene.overlay().showText(55)
			.text("Hold right-click to open the Create-style mode selector")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showText(60)
			.text("Choose single fire, three-round burst or continuous fire in one section")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(70);

		scene.overlay().showText(60)
			.text("Choose polling or salvo separately; polling advances between mounts, while salvo triggers all")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(70);

		scene.overlay().showControls(util.vector().topOf(controller), Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.overlay().showText(50)
			.text("Sneak-right-click selects the next ammunition type")
			.pointAt(util.vector().topOf(controller))
			.placeNearTarget();
		scene.idle(60);

		BlockPos rearWire = controller.north();
		SceneSupport.place(scene, util, rearWire, Blocks.REDSTONE_WIRE.defaultBlockState());
		scene.overlay().showOutline(PonderPalette.RED, "rear_input", util.select().position(rearWire), 55);
		scene.world().toggleRedstonePower(util.select().position(rearWire));
		scene.effects().indicateRedstone(rearWire);
		scene.overlay().showText(55)
			.text("A rising redstone edge on the rear face is equivalent to one short right-click")
			.pointAt(util.vector().centerOf(rearWire))
			.placeNearTarget();
		scene.idle(65);

		BlockPos leftMount = util.grid().at(2, 1, 4);
		BlockPos rightMount = util.grid().at(6, 1, 4);
		BlockPos rack = util.grid().at(4, 1, 6);
		SceneSupport.place(scene, util, leftMount, MTBlocks.COMPACT_AUTOCANNON_MOUNT.get().defaultBlockState());
		SceneSupport.place(scene, util, rightMount, MTBlocks.COMPACT_AUTOCANNON_MOUNT.get().defaultBlockState());
		SceneSupport.place(scene, util, rack, MTBlocks.READY_AMMUNITION_COMPARTMENT.get().defaultBlockState());
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(controller), util.vector().centerOf(leftMount), 65);
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(controller), util.vector().centerOf(rightMount), 65);
		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(controller), util.vector().centerOf(rack), 65);
		scene.overlay().showText(60)
			.text("Bind mounts and ready ammunition equipment without assigning different jobs to different signal faces")
			.pointAt(util.vector().centerOf(controller))
			.placeNearTarget();
		scene.idle(70);

		scene.overlay().showText(55)
			.text("Burst and continuous modes preserve the controlled autocannon's available firing rate")
			.pointAt(util.vector().centerOf(leftMount))
			.placeNearTarget();
		scene.idle(65);

		scene.rotateCameraY(-35);
		scene.overlay().showText(60)
			.text("Ordinary redstone always works; Drive By Wire is only an optional wireless upgrade")
			.pointAt(util.vector().centerOf(rearWire))
			.placeNearTarget();
		scene.idle(70);
		SceneSupport.finish(scene);
	}
}
