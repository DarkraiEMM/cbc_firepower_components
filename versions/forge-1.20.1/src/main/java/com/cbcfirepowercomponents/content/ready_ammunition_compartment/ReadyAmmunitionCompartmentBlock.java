package com.cbcfirepowercomponents.content.ready_ammunition_compartment;

import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.OpenReadyAmmoRackPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ReadyAmmunitionCompartmentBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");

	public ReadyAmmunitionCompartmentBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(OCCUPIED, false));
	}

	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OCCUPIED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level.getBlockEntity(pos) instanceof ReadyAmmunitionCompartmentBlockEntity compartment))
			return InteractionResult.PASS;
		if (!stack.isEmpty()) {
			ItemStack remainder = compartment.insert(stack, false);
			if (remainder.getCount() != stack.getCount()) {
				if (!level.isClientSide && !player.getAbilities().instabuild)
					player.setItemInHand(hand, remainder);
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (player.isShiftKeyDown()) {
			ReadyAmmunitionCompartmentBlockEntity.RoundPair pair = compartment.extractSelectedRound();
			if (pair == null) {
				player.displayClientMessage(Component.translatable("block.cbc_firepower_components.ready_ammunition_compartment.empty"), true);
			} else {
				if (!player.addItem(pair.projectile())) player.drop(pair.projectile(), false);
				if (!player.addItem(pair.propellant())) player.drop(pair.propellant(), false);
			}
		} else {
			MTNetwork.sendToPlayer(player, OpenReadyAmmoRackPacket.from(compartment));
			return InteractionResult.SUCCESS;
		}
		player.displayClientMessage(compartment.getStatusMessage(), true);
		return InteractionResult.SUCCESS;
	}

	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReadyAmmunitionCompartmentBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ReadyAmmunitionCompartmentBlockEntity compartment)
			compartment.dropContents(level);
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
			: createTickerHelper(type, MTBlockEntities.READY_AMMUNITION_COMPARTMENT.get(),
				ReadyAmmunitionCompartmentBlockEntity::tick);
	}
}
