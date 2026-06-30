package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CannonMagazineLoaderRenderer implements BlockEntityRenderer<CannonMagazineLoaderBlockEntity> {
	private static final float FRONT_DISPLAY_Z = 3.55f / 16f;
	private static final float FRONT_DISPLAY_OFFSET = 0.5f - FRONT_DISPLAY_Z;

	private final ItemRenderer itemRenderer;

	public CannonMagazineLoaderRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(CannonMagazineLoaderBlockEntity loader, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
					   int packedLight, int packedOverlay) {
		BlockState state = loader.getBlockState();
		Direction facing = state.getValue(CannonMagazineLoaderBlock.FACING);
		for (int slot = 0; slot < 6; ++slot) {
			ItemStack stack = loader.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			this.renderSlotItem(loader, stack, slot, facing, poseStack, bufferSource, packedLight, packedOverlay);
		}
	}

	private void renderSlotItem(CannonMagazineLoaderBlockEntity loader, ItemStack stack, int slot, Direction facing, PoseStack poseStack,
								MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		int column = slot % 3;
		boolean projectile = slot < 3;
		float x = 0.25f + column * 0.25f;
		float y = projectile ? 0.67f : 0.36f;
		float scale = projectile ? 0.5f : 0.44f;
		Direction right = facing.getClockWise();
		float xOffset = x - 0.5f;
		double worldX = 0.5 + right.getStepX() * xOffset + facing.getStepX() * FRONT_DISPLAY_OFFSET;
		double worldZ = 0.5 + right.getStepZ() * xOffset + facing.getStepZ() * FRONT_DISPLAY_OFFSET;

		poseStack.pushPose();
		poseStack.translate(worldX, y, worldZ);
		poseStack.mulPose(Axis.YP.rotationDegrees(itemRotation(facing)));
		poseStack.scale(scale, scale, scale);
		this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource,
			loader.getLevel(), slot);
		poseStack.popPose();
	}

	private static float itemRotation(Direction facing) {
		return 180.0f - facing.toYRot();
	}
}
