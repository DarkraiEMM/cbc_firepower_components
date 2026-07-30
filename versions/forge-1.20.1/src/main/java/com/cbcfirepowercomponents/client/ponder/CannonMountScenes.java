package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTItems;
import com.simibubi.create.AllBlocks;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public final class CannonMountScenes {
	private CannonMountScenes() {
	}

	public static void compact(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "compact_mounts", "Using Compact Cannon Mounts");
		BlockPos mount = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, mount, MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState());

		scene.overlay().showText(55)
			.text("Compact mounts hold a cannon in less space than a standard mounting")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(65);

		BlockPos shaft = mount.west();
		SceneSupport.place(scene, util, shaft, AllBlocks.SHAFT.getDefaultState());
		scene.overlay().showText(50)
			.text("Connect rotational force through a shaft face to aim the cannon")
			.pointAt(util.vector().centerOf(shaft))
			.placeNearTarget();
		scene.idle(60);

		BlockPos assemblyInput = mount.north();
		BlockPos fireInput = mount.south();
		SceneSupport.place(scene, util, assemblyInput, Blocks.LEVER.defaultBlockState());
		SceneSupport.place(scene, util, fireInput, Blocks.LEVER.defaultBlockState());
		scene.overlay().showOutline(PonderPalette.GREEN, "assembly", util.select().position(assemblyInput), 60);
		scene.overlay().showOutline(PonderPalette.RED, "fire", util.select().position(fireInput), 60);
		scene.overlay().showText(55)
			.text("The opposite redstone faces assemble and fire the mounted cannon")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(65);

		scene.world().setBlock(mount, MTBlocks.COMPACT_AUTOCANNON_MOUNT.get().defaultBlockState(), false);
		scene.overlay().showControls(util.vector().topOf(mount), Pointing.DOWN, 30)
			.withItem(new ItemStack(MTItems.COMPACT_AUTOCANNON_MOUNT.get()));
		scene.overlay().showText(45)
			.text("The autocannon variant uses the same connections but accepts autocannons only")
			.pointAt(util.vector().topOf(mount))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}
}
