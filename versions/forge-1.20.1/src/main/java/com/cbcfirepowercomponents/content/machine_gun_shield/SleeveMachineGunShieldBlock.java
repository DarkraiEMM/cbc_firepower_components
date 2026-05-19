package com.cbcfirepowercomponents.content.machine_gun_shield;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SleeveMachineGunShieldBlock extends HorizontalDirectionalBlock {
	private static final VoxelShape SOUTH_SHAPE = Shapes.or(
		Block.box(1, 3, 14, 15, 10, 15),
		Block.box(1, 13, 14, 15, 15, 15),
		Block.box(1, 10, 14, 5.75, 13, 15),
		Block.box(10.25, 10, 14, 15, 13, 15),
		Block.box(1, 4, 4, 2, 14, 14),
		Block.box(14, 4, 4, 15, 14, 14)
	);
	private static final VoxelShape NORTH_SHAPE = Shapes.or(
		Block.box(1, 3, 1, 15, 10, 2),
		Block.box(1, 13, 1, 15, 15, 2),
		Block.box(1, 10, 1, 5.75, 13, 2),
		Block.box(10.25, 10, 1, 15, 13, 2),
		Block.box(1, 4, 2, 2, 14, 12),
		Block.box(14, 4, 2, 15, 14, 12)
	);
	private static final VoxelShape WEST_SHAPE = Shapes.or(
		Block.box(1, 3, 1, 2, 10, 15),
		Block.box(1, 13, 1, 2, 15, 15),
		Block.box(1, 10, 1, 2, 13, 5.75),
		Block.box(1, 10, 10.25, 2, 13, 15),
		Block.box(2, 4, 1, 12, 14, 2),
		Block.box(2, 4, 14, 12, 14, 15)
	);
	private static final VoxelShape EAST_SHAPE = Shapes.or(
		Block.box(14, 3, 1, 15, 10, 15),
		Block.box(14, 13, 1, 15, 15, 15),
		Block.box(14, 10, 1, 15, 13, 5.75),
		Block.box(14, 10, 10.25, 15, 13, 15),
		Block.box(4, 4, 1, 14, 14, 2),
		Block.box(4, 4, 14, 14, 14, 15)
	);

	public SleeveMachineGunShieldBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			default -> EAST_SHAPE;
		};
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
