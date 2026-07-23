package com.cbcfirepowercomponents.content.large_autocannon;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

public class LargeAutocannonBreechBlock extends AutocannonBreechBlock {
	public LargeAutocannonBreechBlock(Properties properties, AutocannonMaterial material) {
		super(properties, material);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(LargeAutocannonBarrelBlock.CONNECTED_FRONT, false)
			.setValue(LargeAutocannonBarrelBlock.CONNECTED_BACK, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LargeAutocannonBarrelBlock.CONNECTED_FRONT, LargeAutocannonBarrelBlock.CONNECTED_BACK);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : this.updateConnections(state, context.getLevel(), context.getClickedPos());
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
			LargeAutocannonBarrelBlock.refreshConnectionState(level, pos, true);
			level.scheduleTick(pos, this, 1);
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
								  BlockPos currentPos, BlockPos neighborPos) {
		BlockState updated = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
		LargeAutocannonBarrelBlock.scheduleConnectionRefresh(level, currentPos);
		Direction facing = this.getFacing(updated);
		if (direction == facing) {
			return updated.setValue(LargeAutocannonBarrelBlock.CONNECTED_FRONT, this.connectsVisuallyTo(updated, direction, neighborState));
		}
		if (direction == facing.getOpposite()) {
			return updated.setValue(LargeAutocannonBarrelBlock.CONNECTED_BACK, this.connectsVisuallyTo(updated, direction, neighborState));
		}
		return updated;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.tick(state, level, pos, random);
		LargeAutocannonBarrelBlock.refreshConnectionState(level, pos, true);
	}
	private BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
		Direction facing = this.getFacing(state);
		return state
			.setValue(LargeAutocannonBarrelBlock.CONNECTED_FRONT, this.connectsVisuallyTo(state, facing, level.getBlockState(pos.relative(facing))))
			.setValue(LargeAutocannonBarrelBlock.CONNECTED_BACK, this.connectsVisuallyTo(state, facing.getOpposite(), level.getBlockState(pos.relative(facing.getOpposite()))));
	}

	private boolean connectsVisuallyTo(BlockState state, Direction direction, BlockState neighborState) {
		return LargeAutocannonBarrelBlock.canConnectLargeAutocannonVisually(state, direction, neighborState);
	}

	@Override
	public boolean canConnectToSide(BlockState state, Direction direction) {
		return direction == this.getFacing(state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<AbstractAutocannonBreechBlockEntity> getBlockEntityClass() {
		return (Class<AbstractAutocannonBreechBlockEntity>) (Class<?>) LargeAutocannonBreechBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends AbstractAutocannonBreechBlockEntity> getBlockEntityType() {
		return MTBlockEntities.LARGE_AUTOCANNON_BREECH.get();
	}
}
