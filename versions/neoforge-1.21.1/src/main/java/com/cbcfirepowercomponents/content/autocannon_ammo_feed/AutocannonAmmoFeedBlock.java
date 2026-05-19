package com.cbcfirepowercomponents.content.autocannon_ammo_feed;

import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AutocannonAmmoFeedBlock extends BaseEntityBlock {
	public static final MapCodec<AutocannonAmmoFeedBlock> CODEC = simpleCodec(AutocannonAmmoFeedBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE = box(1, 0, 2, 15, 12, 15);

	public AutocannonAmmoFeedBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction linkedDirection = this.findLinkedMount(context.getLevel(), context.getClickedPos());
		return this.defaultBlockState().setValue(FACING, linkedDirection == null ? context.getHorizontalDirection().getOpposite() : linkedDirection);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
		if (level.isClientSide)
			return;
		Direction linkedDirection = this.findLinkedMount(level, pos);
		if (linkedDirection != null && linkedDirection != state.getValue(FACING))
			level.setBlock(pos, state.setValue(FACING, linkedDirection), 3);
	}

	private Direction findLinkedMount(LevelAccessor level, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(pos.relative(direction)).is(MTBlocks.COMPACT_AUTOCANNON_MOUNT.get()))
				return direction;
		}
		return null;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutocannonAmmoFeedBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, MTBlockEntities.AUTOCANNON_AMMO_FEED.get(), AutocannonAmmoFeedBlockEntity::tick);
	}
}
