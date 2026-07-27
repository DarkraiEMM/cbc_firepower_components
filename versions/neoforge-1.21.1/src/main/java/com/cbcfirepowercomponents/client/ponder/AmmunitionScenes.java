package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlock;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class AmmunitionScenes {
	private AmmunitionScenes() {
	}

	public static void autocannonFeed(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "autocannon_feed", "Feeding a Large Autocannon");
		BlockPos box = util.grid().at(2, 1, 4);
		BlockPos feed = util.grid().at(4, 1, 4);
		BlockPos breech = util.grid().at(6, 1, 4);

		SceneSupport.place(scene, util, box, MTBlocks.LARGE_AUTOCANNON_AMMO_BOX.get().defaultBlockState());
		scene.overlay().showText(50)
			.text("Large autocannon ammunition boxes hold loose autocannon rounds")
			.pointAt(util.vector().topOf(box))
			.placeNearTarget();
		scene.idle(60);

		SceneSupport.place(scene, util, feed, MTBlocks.AUTOCANNON_AMMO_FEED.get().defaultBlockState());
		SceneSupport.place(scene, util, breech, MTBlocks.LARGE_AUTOCANNON_BREECH.get().defaultBlockState());
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(box), util.vector().centerOf(breech), 65);
		scene.overlay().showText(55)
			.text("Point the feed path from the box toward the breech")
			.pointAt(util.vector().centerOf(feed))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showControls(util.vector().topOf(box), Pointing.DOWN, 35)
			.withItem(new ItemStack(MTItems.LARGE_AUTOCANNON_HE_ROUND.get()));
		scene.overlay().showText(50)
			.text("Armor-piercing and high-explosive rounds can share the same feed system")
			.pointAt(util.vector().topOf(box))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showText(55)
			.text("A box is consumed one item at a time; stacked boxes are never loaded as one giant magazine")
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
			.text("The magazine loader prepares ammunition before it reaches a cannon")
			.pointAt(util.vector().topOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().of(3.8, 2.0, 4.2), Pointing.DOWN, 35)
			.withItem(new ItemStack(Items.IRON_BLOCK));
		scene.overlay().showText(50)
			.text("Insert one compatible projectile first")
			.pointAt(util.vector().of(3.8, 1.5, 4.2))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showControls(util.vector().of(4.2, 2.0, 4.8), Pointing.DOWN, 35)
			.withItem(new ItemStack(Items.GUNPOWDER));
		scene.overlay().showText(50)
			.text("Then insert the matching propellant charge")
			.pointAt(util.vector().of(4.2, 1.5, 4.8))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(loader),
			util.vector().of(7.0, 1.5, 4.5), 55);
		scene.overlay().showText(55)
			.text("The paired round can then be delivered toward the cannon")
			.pointAt(util.vector().blockSurface(loader, Direction.EAST))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}

	public static void readyCompartment(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "ready_compartment", "Using the Ready Ammunition Compartment");
		BlockPos rack = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, rack, MTBlocks.READY_AMMUNITION_COMPARTMENT.get().defaultBlockState());

		scene.overlay().showText(50)
			.text("Each slot represents one complete firing set, not a stack of loose parts")
			.pointAt(util.vector().topOf(rack))
			.placeNearTarget();
		scene.idle(60);

		for (int i = 0; i < 4; ++i) {
			Vec3 from = util.vector().of(1.2, 2.0, 3.0 + i * 0.7);
			Vec3 to = util.vector().of(3.7, 1.6, 4.5);
			scene.overlay().showLine(PonderPalette.GREEN, from, to, 20);
			scene.world().createItemEntity(from, new Vec3(0.09, 0.02, 0),
				new ItemStack(i % 2 == 0 ? MTItems.LARGE_AUTOCANNON_ROUND.get()
					: MTItems.LARGE_AUTOCANNON_HE_ROUND.get()));
			scene.idle(10);
		}
		scene.overlay().showText(55)
			.text("New rounds enter after the previous tail instead of restarting at the first slot")
			.pointAt(util.vector().centerOf(rack))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showText(55)
			.text("When the leading slot is emptied, the remaining queue advances automatically")
			.pointAt(util.vector().blockSurface(rack, Direction.EAST))
			.placeNearTarget();
		scene.idle(65);

		BlockPos arm = util.grid().at(2, 1, 4);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showText(55)
			.text("Mechanical arms and ordinary item transport may insert or extract complete firing sets")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(65);

		BlockPos mount = util.grid().at(6, 1, 4);
		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState());
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().centerOf(rack), util.vector().centerOf(mount), 65);
		scene.overlay().showText(55)
			.text("An output facing a supported mount supplies it directly; a three-round magazine is optional")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 30).rightClick();
		scene.overlay().showText(45)
			.text("Open the interface to inspect and manually reorder the ammunition queue")
			.pointAt(util.vector().topOf(rack))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void carousel(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "carousel_ammunition_rack", "Using the Carousel Ammunition Rack");
		BlockPos carousel = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, carousel, MTBlocks.CAROUSEL_AMMUNITION_RACK.get().defaultBlockState());
		scene.world().showSection(util.select().fromTo(3, 1, 3, 5, 1, 5), Direction.DOWN);
		scene.idle(12);

		scene.overlay().showOutline(PonderPalette.GREEN, "footprint",
			util.select().fromTo(3, 1, 3, 5, 1, 5), 60);
		scene.overlay().showText(55)
			.text("The carousel occupies a centered 3 by 3 footprint and stores 24 complete rounds")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(3.1, 1.7, 4.0),
			util.vector().of(5.9, 1.7, 4.0), 55);
		scene.overlay().showText(50)
			.text("Rounds lie horizontally around the rotating ring")
			.pointAt(util.vector().centerOf(carousel))
			.placeNearTarget();
		scene.idle(60);

		BlockPos mount = util.grid().at(4, 1, 7);
		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState());
		scene.overlay().showOutline(PonderPalette.OUTPUT, "outlet", util.select().position(4, 1, 5), 65);
		scene.overlay().showText(55)
			.text("The side facing the nearby mount becomes the ammunition outlet")
			.pointAt(util.vector().of(4.5, 1.5, 5.5))
			.placeNearTarget();
		scene.idle(65);

		scene.rotateCameraY(45);
		ElementLink<WorldSectionElement> rotatingRing =
			scene.world().makeSectionIndependent(util.select().fromTo(3, 1, 3, 5, 1, 5));
		scene.world().configureCenterOfRotation(rotatingRing, util.vector().centerOf(carousel));
		scene.world().rotateSection(rotatingRing, 0, 45, 0, 20);
		scene.idle(22);
		scene.overlay().showText(55)
			.text("After the outlet round is removed, the ring advances the next occupied slot automatically")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay().showControls(util.vector().topOf(carousel), Pointing.DOWN, 30).rightClick();
		scene.overlay().showText(50)
			.text("Use the interface to swap positions, or use an empty hand to take the outlet round")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(60);
		SceneSupport.finish(scene);
	}

	public static void spentCollector(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "spent_casing_collector", "Collecting Spent Casings");
		BlockPos collector = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, collector, MTBlocks.SPENT_CASING_COLLECTOR.get().defaultBlockState());

		scene.overlay().showOutline(PonderPalette.GREEN, "range",
			util.select().fromTo(1, 1, 1, 7, 3, 7), 60);
		scene.overlay().showText(55)
			.text("The collector pulls supported spent casings from the surrounding area")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(65);

		scene.world().modifyBlock(collector, state -> state.setValue(SpentCasingCollectorBlock.FILL, 1), false);
		scene.overlay().showText(50)
			.text("Look down through the open top to see a partial pile building inside the chute")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(60);

		scene.world().modifyBlock(collector, state -> state.setValue(SpentCasingCollectorBlock.FILL, 2), false);
		scene.overlay().showText(50)
			.text("A pile near the rim means the collector is almost full")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(60);

		BlockPos arm = util.grid().at(6, 1, 4);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(collector), util.vector().centerOf(arm), 55);
		scene.overlay().showText(55)
			.text("Mechanical arms and other item transport can remove the recovered material")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(65);
		SceneSupport.finish(scene);
	}
}
