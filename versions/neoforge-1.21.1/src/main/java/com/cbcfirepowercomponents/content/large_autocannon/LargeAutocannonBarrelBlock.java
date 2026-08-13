package com.cbcfirepowercomponents.content.large_autocannon;

import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannons.ItemCannonBehavior;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBarrelBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.IAutocannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

import java.util.ArrayList;
import java.util.List;

public class LargeAutocannonBarrelBlock extends AutocannonBarrelBlock {
	public static final BooleanProperty CONNECTED_FRONT = BooleanProperty.create("connected_front");
	public static final BooleanProperty CONNECTED_BACK = BooleanProperty.create("connected_back");
	private static final VoxelShape TWIN_SHAPE_Y = Shapes.or(
		Block.box(0, 0, 4.5, 6.5, 16, 11.5),
		Block.box(9.5, 0, 4.5, 16, 16, 11.5));
	private static final VoxelShape TWIN_SHAPE_Z = Shapes.or(
		Block.box(0, 4.5, 0, 6.5, 11.5, 16),
		Block.box(9.5, 4.5, 0, 16, 11.5, 16));
	private static final VoxelShape TWIN_SHAPE_X = Shapes.or(
		Block.box(0, 4.5, 0, 16, 11.5, 6.5),
		Block.box(0, 4.5, 9.5, 16, 11.5, 16));
	private final boolean twin;

	public LargeAutocannonBarrelBlock(Properties properties, AutocannonMaterial material) {
		this(properties, material, false);
	}

	public LargeAutocannonBarrelBlock(Properties properties, AutocannonMaterial material, boolean twin) {
		super(properties, material);
		this.twin = twin;
		this.registerDefaultState(this.defaultBlockState()
			.setValue(CONNECTED_FRONT, false)
			.setValue(CONNECTED_BACK, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(CONNECTED_FRONT, CONNECTED_BACK);
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
			refreshConnectionState(level, pos, true);
			level.scheduleTick(pos, this, 1);
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
								  BlockPos currentPos, BlockPos neighborPos) {
		BlockState updated = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
		scheduleConnectionRefresh(level, currentPos);
		Direction facing = this.getFacing(updated);
		if (direction == facing) {
			return updated.setValue(CONNECTED_FRONT, this.connectsVisuallyTo(updated, direction, neighborState));
		}
		if (direction == facing.getOpposite()) {
			return updated.setValue(CONNECTED_BACK, this.connectsVisuallyTo(updated, direction, neighborState));
		}
		return updated;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.tick(state, level, pos, random);
		refreshConnectionState(level, pos, true);
	}

	public static void scheduleConnectionRefresh(LevelAccessor level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			BlockState state = serverLevel.getBlockState(pos);
			if ((state.getBlock() instanceof LargeAutocannonBarrelBlock || state.getBlock() instanceof LargeAutocannonBreechBlock)
				&& !serverLevel.getBlockTicks().willTickThisTick(pos, state.getBlock())) {
				serverLevel.scheduleTick(pos, state.getBlock(), 1);
			}
		}
	}
	protected BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
		Direction facing = this.getFacing(state);
		return state
			.setValue(CONNECTED_FRONT, this.connectsVisuallyTo(state, facing, level.getBlockState(pos.relative(facing))))
			.setValue(CONNECTED_BACK, this.connectsVisuallyTo(state, facing.getOpposite(), level.getBlockState(pos.relative(facing.getOpposite()))));
	}

	protected boolean connectsVisuallyTo(BlockState state, Direction direction, BlockState neighborState) {
		return canConnectLargeAutocannonVisually(state, direction, neighborState);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.twin ? twinShape(this.getFacing(state).getAxis()) : super.getShape(state, level, pos, context);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.twin ? twinShape(this.getFacing(state).getAxis()) : super.getCollisionShape(state, level, pos, context);
	}

	static VoxelShape twinShape(Direction.Axis axis) {
		return switch (axis) {
			case X -> TWIN_SHAPE_X;
			case Y -> TWIN_SHAPE_Y;
			case Z -> TWIN_SHAPE_Z;
		};
	}

	@Override
	public boolean canConnectToSide(BlockState state, Direction direction) {
		return direction.getAxis() == this.getFacing(state).getAxis();
	}

	public static void refreshConnectionState(Level level, BlockPos pos, boolean syncCbcBehavior) {
		if (level.isClientSide) return;
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof AutocannonBlock block)) return;
		refreshLine(level, pos, findLargeAutocannonAxis(level, pos, block.getFacing(state).getAxis()), syncCbcBehavior);
	}

	private static Direction.Axis findLargeAutocannonAxis(Level level, BlockPos pos, Direction.Axis fallback) {
		Direction.Axis bestAxis = fallback;
		int bestLength = countLargeAutocannonLine(level, pos, fallback);
		for (Direction.Axis axis : Direction.Axis.values()) {
			int length = countLargeAutocannonLine(level, pos, axis);
			if (length > bestLength) {
				bestAxis = axis;
				bestLength = length;
			}
		}
		return bestAxis;
	}

	private static int countLargeAutocannonLine(Level level, BlockPos pos, Direction.Axis axis) {
		Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		int length = 1;
		BlockPos cursor = pos;
		for (int i = 0; i < 64; ++i) {
			cursor = cursor.relative(negative);
			if (!isLargeAutocannonPart(level.getBlockState(cursor))) break;
			++length;
		}
		cursor = pos;
		for (int i = 0; i < 64; ++i) {
			cursor = cursor.relative(positive);
			if (!isLargeAutocannonPart(level.getBlockState(cursor))) break;
			++length;
		}
		return length;
	}

	private static void refreshLine(Level level, BlockPos origin, Direction.Axis axis, boolean syncCbcBehavior) {
		Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		BlockPos start = origin;
		for (int i = 0; i < 64; ++i) {
			BlockPos next = start.relative(negative);
			if (!isLargeAutocannonPart(level.getBlockState(next))) break;
			start = next;
		}

		List<BlockPos> positions = new ArrayList<>();
		BlockPos cursor = start;
		for (int i = 0; i < 128; ++i) {
			if (!isLargeAutocannonPart(level.getBlockState(cursor))) break;
			positions.add(cursor.immutable());
			cursor = cursor.relative(positive);
		}

		Direction forward = findCannonForward(level, positions, positive, negative);
		if (forward != null) {
			for (BlockPos partPos : positions) {
				orientLargeAutocannonPart(level, partPos, forward);
			}
		}

		if (syncCbcBehavior) {
			for (BlockPos partPos : positions) {
				clearConnectedFaces(level, partPos);
			}
		}

		for (BlockPos partPos : positions) {
			refreshOwnConnectionState(level, partPos, false);
		}
		if (syncCbcBehavior) {
			for (int i = 0; i + 1 < positions.size(); ++i) {
				BlockPos partPos = positions.get(i);
				connectsInLevel(level, level.getBlockState(partPos), partPos, positive, true);
			}
		}
	}

	private static Direction findCannonForward(Level level, List<BlockPos> positions, Direction positive, Direction negative) {
		int breechIndex = -1;
		for (int i = 0; i < positions.size(); ++i) {
			if (level.getBlockState(positions.get(i)).getBlock() instanceof LargeAutocannonBreechBlock) {
				if (breechIndex != -1) {
					return null;
				}
				breechIndex = i;
			}
		}
		if (breechIndex == -1) {
			return null;
		}
		if (breechIndex == 0) {
			return positive;
		}
		if (breechIndex == positions.size() - 1) {
			return negative;
		}
		return null;
	}

	private static void orientLargeAutocannonPart(Level level, BlockPos pos, Direction forward) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof AutocannonBlock block) || block.getFacing(state) == forward) {
			return;
		}
		level.setBlock(pos, state.setValue(FACING, forward), 3);
	}

	private static boolean isLargeAutocannonOnAxis(BlockState state, Direction.Axis axis) {
		if (!(state.getBlock() instanceof AutocannonBlock block)) return false;
		return (state.getBlock() instanceof LargeAutocannonBarrelBlock || state.getBlock() instanceof LargeAutocannonBreechBlock)
			&& block.getFacing(state).getAxis() == axis;
	}

	private static boolean isLargeAutocannonPart(BlockState state) {
		return state.getBlock() instanceof LargeAutocannonBarrelBlock || state.getBlock() instanceof LargeAutocannonBreechBlock;
	}

	private static void refreshOwnConnectionState(Level level, BlockPos pos, boolean syncCbcBehavior) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof AutocannonBlock block)) return;
		Direction facing = block.getFacing(state);
		boolean front = connectsVisuallyInLevel(level, state, pos, facing);
		boolean back = connectsVisuallyInLevel(level, state, pos, facing.getOpposite());
		if (state.hasProperty(CONNECTED_FRONT) && state.hasProperty(CONNECTED_BACK)) {
			BlockState updated = state
				.setValue(CONNECTED_FRONT, front)
				.setValue(CONNECTED_BACK, back);
			if (updated != state) {
				level.setBlock(pos, updated, 3);
				state = level.getBlockState(pos);
				if (!(state.getBlock() instanceof AutocannonBlock updatedBlock)) return;
				block = updatedBlock;
				facing = block.getFacing(state);
			}
		}

		if (syncCbcBehavior) {
			connectsInLevel(level, state, pos, facing, true);
			connectsInLevel(level, state, pos, facing.getOpposite(), true);
		}
	}

	private static boolean connectsInLevel(Level level, BlockState state, BlockPos pos, Direction direction, boolean syncCbcBehavior) {
		if (!(state.getBlock() instanceof AutocannonBlock block) || !canLargePartConnectOn(state, direction)) {
			if (syncCbcBehavior) setConnectedFace(level, pos, direction, false);
			return false;
		}
		BlockPos neighborPos = pos.relative(direction);
		BlockState neighborState = level.getBlockState(neighborPos);
		if (!canConnectLargeAutocannon(state, direction, neighborState)) {
			if (syncCbcBehavior) setConnectedFace(level, pos, direction, false);
			return false;
		}
		AutocannonBlock neighborBlock = (AutocannonBlock) neighborState.getBlock();
		if (neighborBlock.getAutocannonMaterialInLevel(level, neighborState, neighborPos) != block.getAutocannonMaterialInLevel(level, state, pos)) {
			if (syncCbcBehavior) setConnectedFace(level, pos, direction, false);
			return false;
		}
		if (syncCbcBehavior) {
			setConnectedFace(level, pos, direction, true);
			setConnectedFace(level, neighborPos, direction.getOpposite(), true);
		}
		return true;
	}

	private static void clearConnectedFaces(Level level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			setConnectedFace(level, pos, direction, false);
		}
	}

	private static boolean connectsVisuallyInLevel(LevelAccessor level, BlockState state, BlockPos pos, Direction direction) {
		return canConnectLargeAutocannonVisually(state, direction, level.getBlockState(pos.relative(direction)));
	}

	public static boolean canConnectLargeAutocannonVisually(BlockState state, Direction direction, BlockState neighborState) {
		return canConnectLargeAutocannon(state, direction, neighborState);
	}

	public static boolean canConnectLargeAutocannon(BlockState state, Direction direction, BlockState neighborState) {
		if (!(state.getBlock() instanceof AutocannonBlock block) || !canLargePartConnectOn(state, direction)) {
			return false;
		}
		if (neighborState.getBlock() instanceof LargeAutocannonBarrelBlock barrel) {
			return barrel.getFacing(neighborState).getAxis() == direction.getAxis()
				&& canLargePartConnectOn(neighborState, direction.getOpposite());
		}
		if (neighborState.getBlock() instanceof LargeAutocannonBreechBlock breech) {
			return breech.getFacing(neighborState).getAxis() == direction.getAxis()
				&& canLargePartConnectOn(neighborState, direction.getOpposite());
		}
		return false;
	}

	private static boolean canLargePartConnectOn(BlockState state, Direction direction) {
		if (state.getBlock() instanceof LargeAutocannonMuzzleBrakeBlock muzzleBrake) {
			return direction == muzzleBrake.getFacing(state).getOpposite();
		}
		if (state.getBlock() instanceof LargeAutocannonBreechBlock breech) {
			return direction == breech.getFacing(state);
		}
		if (state.getBlock() instanceof LargeAutocannonBarrelBlock barrel) {
			return direction.getAxis() == barrel.getFacing(state).getAxis();
		}
		return false;
	}

	private static void setConnectedFace(Level level, BlockPos pos, Direction direction, boolean connected) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof IAutocannonBlockEntity autocannonBlockEntity
			&& autocannonBlockEntity.cannonBehavior() instanceof ItemCannonBehavior behavior) {
			behavior.setConnectedFace(direction, connected);
			if (blockEntity instanceof SmartBlockEntity smartBlockEntity) {
				smartBlockEntity.notifyUpdate();
			} else {
				blockEntity.setChanged();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<AutocannonBlockEntity> getBlockEntityClass() {
		return (Class<AutocannonBlockEntity>) (Class<?>) LargeAutocannonBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends AutocannonBlockEntity> getBlockEntityType() {
		return MTBlockEntities.LARGE_AUTOCANNON.get();
	}
}
