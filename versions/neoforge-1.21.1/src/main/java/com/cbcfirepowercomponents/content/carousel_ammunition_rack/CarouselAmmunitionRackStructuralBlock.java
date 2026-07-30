package com.cbcfirepowercomponents.content.carousel_ammunition_rack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CarouselAmmunitionRackStructuralBlock extends Block {
	public static final IntegerProperty X_OFFSET = IntegerProperty.create("x_offset", 0, 2);
	public static final IntegerProperty Z_OFFSET = IntegerProperty.create("z_offset", 0, 2);
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

	public CarouselAmmunitionRackStructuralBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(X_OFFSET, 0).setValue(Z_OFFSET, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(X_OFFSET, Z_OFFSET);
	}

	public static BlockPos corePos(BlockPos pos, BlockState state) {
		return pos.offset(1 - state.getValue(X_OFFSET), 0, 1 - state.getValue(Z_OFFSET));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hit) {
		return CarouselAmmunitionRackBlock.insertAt(level, corePos(pos, state), player, hand, stack);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
		BlockHitResult hit) {
		return CarouselAmmunitionRackBlock.openAt(level, corePos(pos, state), player);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && !CarouselAmmunitionRackBlock.isRemovingStructure()) {
			BlockPos core = corePos(pos, state);
			if (level.getBlockState(core).getBlock() instanceof CarouselAmmunitionRackBlock)
				level.destroyBlock(core, true);
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
		CollisionContext context) {
		return SHAPE;
	}
}
