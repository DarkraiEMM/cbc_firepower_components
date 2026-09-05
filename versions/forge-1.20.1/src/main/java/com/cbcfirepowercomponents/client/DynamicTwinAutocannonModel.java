package com.cbcfirepowercomponents.client;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Keeps the connected-texture model available to the block entity renderer while
 * suppressing its static chunk copy. This lets each tube move independently
 * without drawing a stationary duplicate underneath it.
 */
public class DynamicTwinAutocannonModel extends BakedModelWrapper<BakedModel> {
	public DynamicTwinAutocannonModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
		return Collections.emptyList();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
									ModelData modelData, RenderType renderType) {
		return Collections.emptyList();
	}

	public ModelData getDynamicModelData(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return this.originalModel.getModelData(level, pos, state, ModelData.EMPTY);
	}

	public List<BakedQuad> getDynamicQuads(BlockState state, Direction side, RandomSource random,
										  ModelData modelData, RenderType renderType) {
		return this.originalModel.getQuads(state, side, random, modelData, renderType);
	}
}
