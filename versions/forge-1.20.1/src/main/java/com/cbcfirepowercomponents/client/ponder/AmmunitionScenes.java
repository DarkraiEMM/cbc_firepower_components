package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AmmunitionScenes {
	private AmmunitionScenes() {
	}

	public static void autocannonFeed(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "autocannon_feed", "Feeding an Autocannon");
		BlockPos box = util.grid().at(2, 1, 4);
		BlockPos feed = util.grid().at(5, 1, 4);

		SceneSupport.place(scene, util, box, MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get().defaultBlockState());
		scene.overlay().showText(50)
			.text("The ammunition box stores compatible autocannon rounds")
			.pointAt(util.vector().topOf(box))
			.placeNearTarget();
		scene.idle(60);

		SceneSupport.place(scene, util, feed, MTBlocks.AUTOCANNON_AMMO_FEED.get().defaultBlockState());
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(box),
			util.vector().blockSurface(feed, Direction.EAST), 60);
		scene.overlay().showText(55)
			.text("Point the ammunition feed from the box toward the autocannon breech")
			.pointAt(util.vector().centerOf(feed))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showText(55)
			.text("A box is consumed one item at a time; a whole stack is never loaded at once")
			.pointAt(util.vector().centerOf(box))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}

	public static void magazineLoader(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "magazine_loader", "Preparing Complete Cannon Rounds");
		BlockPos loader = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, loader, MTBlocks.CANNON_MAGAZINE_LOADER.get().defaultBlockState());

		scene.overlay().showText(45)
			.text("The loader prepares a complete firing set before it reaches a cannon")
			.pointAt(util.vector().topOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().of(3.8, 2.0, 4.2), Pointing.DOWN, 30)
			.withItem(new ItemStack(Items.IRON_BLOCK));
		scene.overlay().showText(45)
			.text("Insert one compatible projectile")
			.pointAt(util.vector().centerOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().of(4.2, 2.0, 4.8), Pointing.DOWN, 30)
			.withItem(new ItemStack(Items.GUNPOWDER));
		scene.overlay().showText(45)
			.text("Then insert its matching propellant charge")
			.pointAt(util.vector().centerOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showText(50)
			.text("The paired ammunition can now be delivered toward the cannon")
			.pointAt(util.vector().blockSurface(loader, Direction.EAST))
			.placeNearTarget();
		scene.idle(60);
		SceneSupport.finish(scene);
	}
}
