package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBreechBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechRenderer;
import rbasamoyai.createbigcannons.index.CBCBlockPartials;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlock;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;

public class LargeAutocannonBreechRenderer extends AutocannonBreechRenderer {
	private static final float TWIN_BARREL_OFFSET = 0.25f;

	public LargeAutocannonBreechRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(AbstractAutocannonBreechBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
							  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		BlockState state = blockEntity.getBlockState();
		if (blockEntity instanceof LargeAutocannonBreechBlockEntity largeBreech) {
			TwinLargeAutocannonModelRenderer.render(largeBreech, largeBreech, partialTicks,
				poseStack, bufferSource, packedLight, packedOverlay);
		}
		if (!(blockEntity instanceof LargeAutocannonBreechBlockEntity largeBreech)
			|| !state.is(MTBlocks.TWIN_LARGE_AUTOCANNON_BREECH.get())
			|| state.getValue(AutocannonBreechBlock.HANDLE)) {
			super.renderSafe(blockEntity, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);
			return;
		}

		Direction facing = state.getValue(AutocannonBreechBlock.FACING);
		this.renderTwinEjector(largeBreech, false, facing, partialTicks, poseStack, bufferSource, packedLight);
		this.renderTwinEjector(largeBreech, true, facing, partialTicks, poseStack, bufferSource, packedLight);
		this.renderMagazine(blockEntity, facing, poseStack, bufferSource, packedLight);
	}

	private void renderTwinEjector(LargeAutocannonBreechBlockEntity breech, boolean rightBarrel, Direction facing,
								   float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (!(breech.getBlockState().getBlock() instanceof AutocannonBlock autocannonBlock))
			return;

		Direction right = facing.getAxis().isVertical() ? Direction.EAST : facing.getClockWise();
		float sideOffset = rightBarrel ? TWIN_BARREL_OFFSET : -TWIN_BARREL_OFFSET;
		float recoilOffset = breech.getTwinAnimateOffset(rightBarrel, partialTicks) * -0.5f;
		Vector3f translation = new Vector3f(
			right.getStepX() * sideOffset + facing.getStepX() * recoilOffset,
			right.getStepY() * sideOffset + facing.getStepY() * recoilOffset,
			right.getStepZ() * sideOffset + facing.getStepZ() * recoilOffset
		);

		SuperByteBuffer ejector = CachedBuffers.partialFacing(
			CBCBlockPartials.autocannonEjectorFor(autocannonBlock.getAutocannonMaterial()),
			breech.getBlockState(),
			facing
		);
		ejector.translate(translation)
			.rotateCentered(Axis.YP.rotationDegrees(facing.getAxis().isVertical() ? 180.0f : 0.0f))
			.light(packedLight)
			.renderInto(poseStack, bufferSource.getBuffer(RenderType.cutoutMipped()));
	}

	private void renderMagazine(AbstractAutocannonBreechBlockEntity breech, Direction facing, PoseStack poseStack,
								MultiBufferSource bufferSource, int packedLight) {
		ItemStack magazine = breech.getMagazine();
		if (!(magazine.getItem() instanceof AutocannonAmmoContainerItem)
			|| !(magazine.getItem() instanceof BlockItem blockItem))
			return;

		BlockState magazineState = blockItem.getBlock().defaultBlockState();
		if (magazineState.hasProperty(AutocannonAmmoContainerBlock.CONTAINER_STATE)) {
			boolean filled = AutocannonAmmoContainerItem.getTotalAmmoCount(magazine) > 0;
			magazineState = magazineState.setValue(AutocannonAmmoContainerBlock.CONTAINER_STATE,
				AutocannonAmmoContainerBlock.State.getFromFilled(filled));
		}

		boolean vertical = facing.getAxis().isVertical();
		Quaternionf rotation;
		if (vertical) {
			float angle = facing == Direction.UP ? 90.0f : -90.0f;
			rotation = Axis.ZP.rotationDegrees(angle);
			rotation.mul(Axis.XP.rotationDegrees(angle));
		} else {
			rotation = Axis.YP.rotationDegrees(-90.0f - facing.toYRot());
		}

		Direction side = vertical
			? facing.getCounterClockWise(Direction.Axis.Z)
			: facing.getClockWise(Direction.Axis.Y);
		Vector3f translation = (facing == Direction.UP ? side.getOpposite() : side).step().mul(0.625f);
		CachedBuffers.block(magazineState)
			.translate(translation)
			.rotateCentered(rotation)
			.light(packedLight)
			.renderInto(poseStack, bufferSource.getBuffer(RenderType.cutoutMipped()));
	}
}
