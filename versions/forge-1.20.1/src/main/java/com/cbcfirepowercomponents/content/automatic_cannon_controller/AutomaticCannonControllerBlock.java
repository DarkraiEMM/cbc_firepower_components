package com.cbcfirepowercomponents.content.automatic_cannon_controller;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;

public class AutomaticCannonControllerBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public AutomaticCannonControllerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return this.openController(level, pos, player);
	}

	private InteractionResult openController(Level level, BlockPos pos, Player player) {
		if (!(level.getBlockEntity(pos) instanceof AutomaticCannonControllerBlockEntity controller))
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (player.isShiftKeyDown()) {
			controller.cancelPendingInteraction(player);
			controller.openAmmunitionSelection(player);
		} else {
			controller.cancelPendingInteraction(player);
			controller.openConfiguration(player);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moving) {
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof AutomaticCannonControllerBlockEntity controller)
			controller.refreshRedstoneCommands();
	}

	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutomaticCannonControllerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
			: createTickerHelper(type, MTBlockEntities.AUTOMATIC_CANNON_CONTROLLER.get(),
				AutomaticCannonControllerBlockEntity::tick);
	}
}
