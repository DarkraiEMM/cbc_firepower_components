package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class EquipmentScenes {
	private EquipmentScenes() {
	}

	public static void limiter(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "cannon_limiter", "Limiting Cannon Movement");
		BlockPos mount = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState());

		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 35)
			.withItem(new ItemStack(MTItems.CANNON_LIMITER.get()))
			.rightClick();
		scene.overlay().showText(55)
			.text("Use the limiter on a compact mount to configure a safe aiming envelope")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(2.5, 2.4, 4.5),
			util.vector().of(6.5, 2.4, 4.5), 55);
		scene.overlay().showText(50)
			.text("The selected yaw and pitch limits prevent the cannon from moving outside the chosen arc")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.overlay().showText(45)
			.text("Sneak-right-click the mount to remove an installed limiter")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void rangefinding(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "spyglass_rangefinding", "Measuring Distance with a Spyglass");
		BlockPos observer = util.grid().at(1, 1, 4);
		BlockPos target = util.grid().at(7, 1, 4);
		SceneSupport.place(scene, util, observer, Blocks.LECTERN.defaultBlockState());
		SceneSupport.place(scene, util, target, Blocks.TARGET.defaultBlockState());

		scene.overlay().showControls(util.vector().topOf(observer), Pointing.DOWN, 35)
			.withItem(new ItemStack(Items.SPYGLASS));
		scene.overlay().showText(55)
			.text("While using a spyglass, press the configured rangefinding key")
			.pointAt(util.vector().topOf(observer))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showBigLine(PonderPalette.GREEN, util.vector().of(1.5, 2.0, 4.5),
			util.vector().centerOf(target), 65);
		scene.overlay().showText(55)
			.text("The result is the straight-line distance to the nearest point hit by your view")
			.pointAt(util.vector().centerOf(target))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showText(60)
			.text("Targets are not identified, locked or stored")
			.pointAt(util.vector().centerOf(target))
			.placeNearTarget();
		scene.idle(70);

		scene.rotateCameraY(30);
		scene.overlay().showText(60)
			.text("On Aeronautics physics bodies, the measurement uses actual world-space positions instead of local coordinates")
			.pointAt(util.vector().of(4.5, 2.2, 4.5))
			.placeNearTarget();
		scene.idle(70);
		SceneSupport.finish(scene);
	}
}
