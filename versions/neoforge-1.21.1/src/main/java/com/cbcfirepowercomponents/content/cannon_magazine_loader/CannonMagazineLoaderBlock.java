package com.cbcfirepowercomponents.content.cannon_magazine_loader;

import com.mojang.serialization.MapCodec;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CannonMagazineLoaderBlock extends BaseEntityBlock {
	public static final MapCodec<CannonMagazineLoaderBlock> CODEC = simpleCodec(CannonMagazineLoaderBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_NORTH = makeShape(Direction.NORTH);
	private static final VoxelShape SHAPE_SOUTH = makeShape(Direction.SOUTH);
	private static final VoxelShape SHAPE_EAST = makeShape(Direction.EAST);
	private static final VoxelShape SHAPE_WEST = makeShape(Direction.WEST);

	public CannonMagazineLoaderBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction linkedDirection = this.findLinkedMount(context.getLevel(), context.getClickedPos());
		return this.defaultBlockState().setValue(FACING, linkedDirection == null ? context.getHorizontalDirection().getOpposite() : linkedDirection);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
		if (level.isClientSide)
			return;
		Direction linkedDirection = this.findLinkedMount(level, pos);
		if (linkedDirection != null && linkedDirection != state.getValue(FACING))
			level.setBlock(pos, state.setValue(FACING, linkedDirection), 3);
	}

	private Direction findLinkedMount(LevelAccessor level, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(pos.relative(direction)).is(MTBlocks.COMPACT_CANNON_MOUNT.get()))
				return direction;
		}
		return null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getOrientedShape(state);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getOrientedShape(state);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
											 InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CannonMagazineLoaderBlockEntity loader))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		ItemStack remainder = loader.insertManual(stack);
		if (remainder.getCount() == stack.getCount())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide && !player.getAbilities().instabuild)
			player.setItemInHand(hand, remainder);
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CannonMagazineLoaderBlockEntity loader))
			return InteractionResult.PASS;
		int slot = getSlotFromHit(state, pos, hit);
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		ItemStack extracted = loader.extractManual(slot);
		if (extracted.isEmpty())
			return InteractionResult.PASS;
		if (!player.addItem(extracted))
			player.drop(extracted, false);
		return InteractionResult.SUCCESS;
	}

	private static int getSlotFromHit(BlockState state, BlockPos pos, BlockHitResult hit) {
		Vec3 local = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
		double x = local.x;
		Direction facing = state.getValue(FACING);
		double modelX = switch (facing) {
			case SOUTH -> 1 - x;
			case EAST -> local.z;
			case WEST -> 1 - local.z;
			default -> x;
		};
		int column = Mth.clamp((int) (modelX * 3), 0, 2);
		return local.y < 0.5 ? 3 + column : column;
	}

	private static VoxelShape getOrientedShape(BlockState state) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			case WEST -> SHAPE_WEST;
			default -> SHAPE_NORTH;
		};
	}

	private static VoxelShape makeShape(Direction facing) {
		return orientedBox(facing, 0, 0, 4, 16, 16, 16);
	}

	private static VoxelShape orientedBox(Direction facing, double x1, double y1, double z1, double x2, double y2, double z2) {
		return switch (facing) {
			case SOUTH -> box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
			case EAST -> box(16 - z2, y1, x1, 16 - z1, y2, x2);
			case WEST -> box(z1, y1, 16 - x2, z2, y2, 16 - x1);
			default -> box(x1, y1, z1, x2, y2, z2);
		};
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CannonMagazineLoaderBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), CannonMagazineLoaderBlockEntity::tick);
	}
}
