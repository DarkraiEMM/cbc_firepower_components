package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlock;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CarouselAmmunitionRackRenderer implements BlockEntityRenderer<CarouselAmmunitionRackBlockEntity> {
	private final ItemRenderer itemRenderer;

	public CarouselAmmunitionRackRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(CarouselAmmunitionRackBlockEntity rack, float partialTick, PoseStack poseStack,
		MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		Direction facing = rack.getBlockState().getValue(CarouselAmmunitionRackBlock.FACING);
		double outletAngle = Math.atan2(facing.getStepZ(), facing.getStepX());
		float visualIndex = rack.getVisualIndex(partialTick);
		for (int slot = 0; slot < CarouselAmmunitionRackBlockEntity.CAPACITY; ++slot) {
			ItemStack projectile = rack.getProjectileForRender(slot);
			ItemStack propellant = rack.getPropellantForRender(slot);
			if (projectile.isEmpty() && propellant.isEmpty())
				continue;
			double angle = outletAngle + Math.PI * 2.0 * (slot - visualIndex)
				/ CarouselAmmunitionRackBlockEntity.CAPACITY;
			if (!projectile.isEmpty())
				this.renderItem(rack, projectile, slot, angle, 1.08, 0.69, 0.36f,
					poseStack, bufferSource, packedLight, packedOverlay);
			if (!propellant.isEmpty())
				this.renderItem(rack, propellant, slot + 31, angle, 0.78, 0.66, 0.29f,
					poseStack, bufferSource, packedLight, packedOverlay);
		}
	}

	private void renderItem(CarouselAmmunitionRackBlockEntity rack, ItemStack stack, int seed, double angle,
		double radius, double y, float scale, PoseStack poseStack, MultiBufferSource bufferSource,
		int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5 + Math.cos(angle) * radius, y, 0.5 + Math.sin(angle) * radius);
		poseStack.mulPose(Axis.YP.rotation((float) (-angle + Math.PI / 2)));
		poseStack.mulPose(Axis.XP.rotationDegrees(90));
		poseStack.scale(scale, scale, scale);
		this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
			poseStack, bufferSource, rack.getLevel(), seed);
		poseStack.popPose();
	}

	@Override public boolean shouldRenderOffScreen(CarouselAmmunitionRackBlockEntity rack) {
		return true;
	}
}
