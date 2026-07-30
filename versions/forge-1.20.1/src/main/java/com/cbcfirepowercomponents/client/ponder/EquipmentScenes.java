package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

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
			.text("The selected yaw and pitch limits keep the cannon inside the chosen arc")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.overlay().showText(45)
			.text("Sneak-right-click the mount to remove the limiter")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void shield(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "machine_gun_shield", "Installing the Sleeve Machine Gun Shield");
		BlockPos shield = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, shield, MTBlocks.SLEEVE_MACHINE_GUN_SHIELD.get().defaultBlockState());

		scene.overlay().showText(50)
			.text("The shield is installed around the supported machine-gun barrel")
			.pointAt(util.vector().centerOf(shield))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showOutline(PonderPalette.GREEN, "opening", util.select().position(shield), 55);
		scene.overlay().showText(55)
			.text("Align the sleeve opening with the barrel instead of placing the shield as a separate wall")
			.pointAt(util.vector().centerOf(shield))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}
}
