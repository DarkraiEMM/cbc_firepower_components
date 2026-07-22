package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.content.large_autocannon.LargeAutocannonBarrelBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.connected_textures.CBCCTSpriteShifter;
import rbasamoyai.createbigcannons.index.CBCCTTypes;
import rbasamoyai.createbigcannons.index.CBCSpriteShifts;

/**
 * Applies CBC's cannon connected-texture sheet to the large autocannon tube.
 * The connection test deliberately delegates to the gameplay connection rules,
 * so barrels also blend when the adjacent part is a breech or muzzle brake.
 */
public class LargeAutocannonCTBehaviour extends ConnectedTextureBehaviour.Base {
	private static final CTSpriteShiftEntry STEEL_BARREL_SHIFT = CBCSpriteShifts.STEEL_CANNON_BARREL;
	private static final ResourceLocation CONNECTED_STEEL = ResourceLocation.fromNamespaceAndPath("createbigcannons",
		"block/cannon_barrel/steel_cannon_barrel_side_connected");
	private static final CTSpriteShiftEntry MUZZLE_BRAKE_SHIFT = localShift("steel_large_autocannon_muzzle_brake_side");

	private static CTSpriteShiftEntry localShift(String texture) {
		return CBCCTSpriteShifter.getCT(CBCCTTypes.CANNON, 1,
			ResourceLocation.fromNamespaceAndPath("cbc_firepower_components", "block/large_autocannon/" + texture),
			CONNECTED_STEEL);
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction face, TextureAtlasSprite sprite) {
		Direction facing = state.getValue(BlockStateProperties.FACING);
		if (face.getAxis() == facing.getAxis())
			return null;
		if (sprite == null)
			return STEEL_BARREL_SHIFT;
		ResourceLocation texture = sprite.contents().name();
		if (texture.equals(STEEL_BARREL_SHIFT.getOriginal().contents().name()))
			return STEEL_BARREL_SHIFT;
		if (texture.equals(MUZZLE_BRAKE_SHIFT.getOriginal().contents().name()))
			return MUZZLE_BRAKE_SHIFT;
		return null;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState otherState, BlockAndTintGetter level,
		BlockPos pos, BlockPos otherPos, Direction face) {
		if (pos.distManhattan(otherPos) != 1)
			return false;
		BlockPos offset = otherPos.subtract(pos);
		Direction connectionDirection = Direction.getNearest(offset.getX(), offset.getY(), offset.getZ());
		return LargeAutocannonBarrelBlock.canConnectLargeAutocannonVisually(state, connectionDirection, otherState);
	}

	@Override
	protected Direction getUpDirection(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
		Direction.Axis cannonAxis = state.getValue(BlockStateProperties.FACING).getAxis();
		return Direction.fromAxisAndDirection(cannonAxis, Direction.AxisDirection.POSITIVE);
	}

	@Override
	protected Direction getRightDirection(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
		Direction.Axis cannonAxis = state.getValue(BlockStateProperties.FACING).getAxis();
		return face.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? face.getClockWise(cannonAxis)
			: face.getCounterClockWise(cannonAxis);
	}

	@Override
	public CTContext buildContext(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face,
		ContextRequirement contextRequirement) {
		CTContext context = super.buildContext(level, pos, state, face, contextRequirement);
		if (state.getValue(BlockStateProperties.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE) {
			boolean up = context.up;
			context.up = context.down;
			context.down = up;
		}
		return context;
	}
}
