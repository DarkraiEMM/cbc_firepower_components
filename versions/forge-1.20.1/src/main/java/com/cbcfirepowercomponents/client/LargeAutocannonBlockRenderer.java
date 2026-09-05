package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LargeAutocannonBlockRenderer implements BlockEntityRenderer<LargeAutocannonBlockEntity> {
	public LargeAutocannonBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(LargeAutocannonBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
					   MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		TwinLargeAutocannonModelRenderer.render(blockEntity, blockEntity, partialTicks,
			poseStack, bufferSource, packedLight, packedOverlay);
	}
}
