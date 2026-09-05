package com.cbcfirepowercomponents.content.large_autocannon;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

public class LargeAutocannonMuzzleBrakeBlock extends LargeAutocannonBarrelBlock {
	private static final VoxelShape SINGLE_SHAPE_Y = Block.box(4, 0, 4, 12, 16, 12);
	private static final VoxelShape SINGLE_SHAPE_Z = Block.box(4, 4, 0, 12, 12, 16);
	private static final VoxelShape SINGLE_SHAPE_X = Block.box(0, 4, 4, 16, 12, 12);
	private static final VoxelShape TWIN_SHAPE_Y = Shapes.or(
		Block.box(-1, 0, 4, 7, 16, 12),
		Block.box(9, 0, 4, 17, 16, 12));
	private static final VoxelShape TWIN_SHAPE_Z = Shapes.or(
		Block.box(-1, 4, 0, 7, 12, 16),
		Block.box(9, 4, 0, 17, 12, 16));
	private static final VoxelShape TWIN_SHAPE_X = Shapes.or(
		Block.box(0, 4, -1, 16, 12, 7),
		Block.box(0, 4, 9, 16, 12, 17));
	private final boolean twin;

	public LargeAutocannonMuzzleBrakeBlock(Properties properties, AutocannonMaterial material) {
		this(properties, material, false);
	}

	public LargeAutocannonMuzzleBrakeBlock(Properties properties, AutocannonMaterial material, boolean twin) {
		super(properties, material, twin);
		this.twin = twin;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return muzzleShape(this.getFacing(state).getAxis());
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return muzzleShape(this.getFacing(state).getAxis());
	}

	private VoxelShape muzzleShape(Direction.Axis axis) {
		if (!this.twin) {
			return switch (axis) {
				case X -> SINGLE_SHAPE_X;
				case Y -> SINGLE_SHAPE_Y;
				case Z -> SINGLE_SHAPE_Z;
			};
		}
		return switch (axis) {
			case X -> TWIN_SHAPE_X;
			case Y -> TWIN_SHAPE_Y;
			case Z -> TWIN_SHAPE_Z;
		};
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) return null;
		state = state.setValue(FACING, context.getClickedFace());
		return this.updateConnections(state, context.getLevel(), context.getClickedPos());
	}

	@Override
	public boolean canConnectToSide(BlockState state, Direction direction) {
		return direction == this.getFacing(state).getOpposite();
	}
}
