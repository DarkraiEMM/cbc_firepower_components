package com.cbcfirepowercomponents.content.carousel_ammunition_rack;

import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.OpenCarouselRackPacket;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CarouselAmmunitionRackBlock extends KineticBlock implements IBE<CarouselAmmunitionRackBlockEntity> {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty INDEXING = BooleanProperty.create("indexing");
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);
	private static boolean removingStructure;

	public CarouselAmmunitionRackBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(INDEXING, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, INDEXING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos center = context.getClickedPos();
		for (int x = -1; x <= 1; ++x)
			for (int z = -1; z <= 1; ++z)
				if ((x != 0 || z != 0)
					&& !context.getLevel().getBlockState(center.offset(x, 0, z)).canBeReplaced())
					return null;
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
		super.onPlace(state, level, pos, oldState, moving);
		if (level.isClientSide || oldState.is(this))
			return;
		for (int x = -1; x <= 1; ++x)
			for (int z = -1; z <= 1; ++z) {
				if (x == 0 && z == 0)
					continue;
				level.setBlock(pos.offset(x, 0, z), MTBlocks.CAROUSEL_AMMUNITION_RACK_STRUCTURE.get()
					.defaultBlockState()
					.setValue(CarouselAmmunitionRackStructuralBlock.X_OFFSET, x + 1)
					.setValue(CarouselAmmunitionRackStructuralBlock.Z_OFFSET, z + 1), 3);
			}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof CarouselAmmunitionRackBlockEntity rack)
				rack.dropContents();
			removingStructure = true;
			try {
				for (int x = -1; x <= 1; ++x)
					for (int z = -1; z <= 1; ++z)
						if ((x != 0 || z != 0)
							&& level.getBlockState(pos.offset(x, 0, z)).is(MTBlocks.CAROUSEL_AMMUNITION_RACK_STRUCTURE.get()))
							level.removeBlock(pos.offset(x, 0, z), false);
			} finally {
				removingStructure = false;
			}
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	static boolean isRemovingStructure() {
		return removingStructure;
	}

	static InteractionResult insertAt(Level level, BlockPos corePos, Player player, InteractionHand hand, ItemStack stack) {
		if (!(level.getBlockEntity(corePos) instanceof CarouselAmmunitionRackBlockEntity rack))
			return InteractionResult.PASS;
		ItemStack remainder = rack.insert(stack, false);
		if (remainder.getCount() == stack.getCount())
			return InteractionResult.PASS;
		if (!level.isClientSide && !player.getAbilities().instabuild)
			player.setItemInHand(hand, remainder);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	static InteractionResult openAt(Level level, BlockPos corePos, Player player) {
		if (!(level.getBlockEntity(corePos) instanceof CarouselAmmunitionRackBlockEntity rack))
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (player.isShiftKeyDown()) {
			var pair = rack.extractAlignedRound();
			if (pair == null) {
				player.displayClientMessage(Component.translatable(
					"block.cbc_firepower_components.ready_ammunition_compartment.empty"), true);
			} else {
				if (!pair.projectile().isEmpty() && !player.addItem(pair.projectile()))
					player.drop(pair.projectile(), false);
				if (!pair.propellant().isEmpty() && !player.addItem(pair.propellant()))
					player.drop(pair.propellant(), false);
			}
		} else {
			MTNetwork.sendToPlayer(player, OpenCarouselRackPacket.from(rack));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.isEmpty()) {
			InteractionResult inserted = insertAt(level, pos, player, hand, stack);
			if (inserted.consumesAction())
				return inserted;
		}
		return openAt(level, pos, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
		CollisionContext context) {
		return SHAPE;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override public Class<CarouselAmmunitionRackBlockEntity> getBlockEntityClass() {
		return CarouselAmmunitionRackBlockEntity.class;
	}
	@Override public BlockEntityType<? extends CarouselAmmunitionRackBlockEntity> getBlockEntityType() {
		return MTBlockEntities.CAROUSEL_AMMUNITION_RACK.get();
	}
}
