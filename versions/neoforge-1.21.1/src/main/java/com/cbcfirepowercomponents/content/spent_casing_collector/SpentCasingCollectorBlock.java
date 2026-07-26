package com.cbcfirepowercomponents.content.spent_casing_collector;

import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public class SpentCasingCollectorBlock extends BaseEntityBlock {
	public static final MapCodec<SpentCasingCollectorBlock> CODEC = simpleCodec(SpentCasingCollectorBlock::new);
	public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 2);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	public SpentCasingCollectorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FILL, 0)
			.setValue(FACING, Direction.NORTH));
	}

	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FILL, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof SpentCasingCollectorBlockEntity collector))
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		ItemStack stack = collector.extractOne(false);
		if (stack.isEmpty())
			return InteractionResult.PASS;
		if (!player.addItem(stack))
			player.drop(stack, false);
		return InteractionResult.SUCCESS;
	}

	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SpentCasingCollectorBlockEntity(pos, state);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SpentCasingCollectorBlockEntity collector)
			collector.dropContents(level);
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
			: createTickerHelper(type, MTBlockEntities.SPENT_CASING_COLLECTOR.get(), SpentCasingCollectorBlockEntity::tick);
	}
}
