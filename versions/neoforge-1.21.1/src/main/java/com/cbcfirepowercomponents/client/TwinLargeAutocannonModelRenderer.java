package com.cbcfirepowercomponents.client;

import java.util.List;

import com.cbcfirepowercomponents.content.large_autocannon.TwinLargeAutocannonRecoilSource;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class TwinLargeAutocannonModelRenderer {
	private static final float FULL_RECOIL_DISTANCE = 0.22f;
	private static final int VERTEX_STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / Integer.BYTES;

	private TwinLargeAutocannonModelRenderer() {
	}

	public static void render(BlockEntity blockEntity, TwinLargeAutocannonRecoilSource recoilSource, float partialTicks,
							  PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		if (blockEntity.getLevel() == null)
			return;

		BlockState state = blockEntity.getBlockState();
		BakedModel bakedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
		if (!(bakedModel instanceof DynamicTwinAutocannonModel dynamicModel))
			return;

		Direction facing = state.getValue(BlockStateProperties.FACING);
		Direction right = facing.getAxis().isVertical() ? Direction.EAST : facing.getClockWise();
		boolean twin = isTwinPart(state);
		ModelData modelData = dynamicModel.getDynamicModelData(
			blockEntity.getLevel(), blockEntity.getBlockPos(), state);
		RenderType renderType = RenderType.cutoutMipped();
		VertexConsumer consumer = bufferSource.getBuffer(renderType);
		RandomSource random = RandomSource.create(42L);

		for (Direction side : Direction.values()) {
			random.setSeed(42L);
			renderQuads(dynamicModel.getDynamicQuads(state, side, random, modelData, renderType), twin, right, facing,
				recoilSource, partialTicks, poseStack, consumer, packedLight, packedOverlay);
		}
		random.setSeed(42L);
		renderQuads(dynamicModel.getDynamicQuads(state, null, random, modelData, renderType), twin, right, facing,
			recoilSource, partialTicks, poseStack, consumer, packedLight, packedOverlay);
	}

	private static void renderQuads(List<BakedQuad> quads, boolean twin, Direction right, Direction facing,
									TwinLargeAutocannonRecoilSource recoilSource, float partialTicks,
									PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
		for (BakedQuad quad : quads) {
			boolean rightBarrel = twin && isRightBarrel(quad, right);
			float recoil = -FULL_RECOIL_DISTANCE * recoilSource.getTwinAnimateOffset(rightBarrel, partialTicks);
			poseStack.pushPose();
			poseStack.translate(
				facing.getStepX() * recoil,
				facing.getStepY() * recoil,
				facing.getStepZ() * recoil
			);
			consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
			poseStack.popPose();
		}
	}

	private static boolean isTwinPart(BlockState state) {
		return state.is(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get())
			|| state.is(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_BARREL.get())
			|| state.is(MTBlocks.STEEL_TWIN_LARGE_AUTOCANNON_MUZZLE_BRAKE.get());
	}

	private static boolean isRightBarrel(BakedQuad quad, Direction right) {
		int[] vertices = quad.getVertices();
		float lateral = 0.0f;
		for (int vertex = 0; vertex < 4; ++vertex) {
			int offset = vertex * VERTEX_STRIDE;
			float x = Float.intBitsToFloat(vertices[offset]);
			float y = Float.intBitsToFloat(vertices[offset + 1]);
			float z = Float.intBitsToFloat(vertices[offset + 2]);
			lateral += (x - 0.5f) * right.getStepX()
				+ (y - 0.5f) * right.getStepY()
				+ (z - 0.5f) * right.getStepZ();
		}
		return lateral >= 0.0f;
	}
}
