package com.cbcfirepowercomponents.client.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

final class SceneSupport {
	private SceneSupport() {
	}

	static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title) {
		scene.title(id, title);
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(0.82f);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);
	}

	static void place(SceneBuilder scene, SceneBuildingUtil util, BlockPos pos, BlockState state) {
		scene.world().setBlock(pos, state, false);
		scene.world().showSection(util.select().position(pos), Direction.DOWN);
		scene.idle(8);
	}

	static void finish(SceneBuilder scene) {
		scene.idle(20);
		scene.markAsFinished();
	}
}
