package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.registry.MTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CompactCannonMountLimiterRenderer implements BlockEntityRenderer<CompactCannonMountBlockEntity> {
	private final ItemRenderer itemRenderer;

	public CompactCannonMountLimiterRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(CompactCannonMountBlockEntity mount, float partialTick, PoseStack poseStack,
					   MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		if (!mount.hasLimiter())
			return;

		BlockState state = mount.getBlockState();
		Direction facing = state.getValue(CompactCannonMountBlock.HORIZONTAL_FACING);
		ItemStack stack = mount.getLimiterStack().isEmpty()
			? new ItemStack(MTItems.CANNON_LIMITER.get())
			: mount.getLimiterStack();

		poseStack.pushPose();
		poseStack.translate(0.5f, 1.04f, 0.5f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - facing.toYRot()));
		poseStack.translate(0.0f, 0.02f, -0.24f);
		poseStack.scale(0.58f, 0.58f, 0.58f);
		this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay,
			poseStack, bufferSource, mount.getLevel(), 0);
		poseStack.popPose();
	}
}
