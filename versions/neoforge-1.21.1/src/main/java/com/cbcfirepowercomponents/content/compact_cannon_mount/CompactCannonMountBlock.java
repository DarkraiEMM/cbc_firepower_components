package com.cbcfirepowercomponents.content.compact_cannon_mount;

import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlock;

public class CompactCannonMountBlock extends KineticBlock implements IBE<CompactCannonMountBlockEntity> {

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
	public static final BooleanProperty ASSEMBLY_POWERED = CannonMountBlock.ASSEMBLY_POWERED;
	public static final BooleanProperty FIRE_POWERED = CannonMountBlock.FIRE_POWERED;
	public static final BooleanProperty SIGNALS_REVERSED = BooleanProperty.create("signals_reversed");

	public CompactCannonMountBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(HORIZONTAL_FACING, Direction.NORTH)
			.setValue(VERTICAL_DIRECTION, Direction.DOWN)
			.setValue(ASSEMBLY_POWERED, false)
			.setValue(FIRE_POWERED, false)
			.setValue(SIGNALS_REVERSED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HORIZONTAL_FACING, VERTICAL_DIRECTION, ASSEMBLY_POWERED, FIRE_POWERED, SIGNALS_REVERSED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
			.setValue(HORIZONTAL_FACING, context.getHorizontalDirection())
			.setValue(VERTICAL_DIRECTION, context.getNearestLookingVerticalDirection());
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getAxis() == Axis.X ? Axis.Z : Axis.X;
	}

	public Direction getPitchInputDirection(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getClockWise();
	}

	public Direction getCannonSide(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getCounterClockWise();
	}

	public boolean isAutocannonOnly() {
		return false;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == this.getRotationAxis(state) || face.getAxis().isVertical();
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(HORIZONTAL_FACING, mirror.mirror(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
		if (!level.isClientSide && !level.getBlockTicks().willTickThisTick(pos, this)) {
			level.scheduleTick(pos, this, 0);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		boolean prevAssemblyPowered = state.getValue(ASSEMBLY_POWERED);
		boolean prevFirePowered = state.getValue(FIRE_POWERED);
		boolean assemblyPowered = this.hasNeighborSignal(level, state, pos, ASSEMBLY_POWERED);
		boolean firePowered = this.hasNeighborSignal(level, state, pos, FIRE_POWERED);
		Direction fireDirection = this.getFireSignalDirection(state);
		int firePower = level.getSignal(pos.relative(fireDirection), fireDirection);
		this.withBlockEntityDo(level, pos, mount -> mount.onRedstoneUpdate(assemblyPowered, prevAssemblyPowered, firePowered, prevFirePowered, firePower));
	}

	private boolean hasNeighborSignal(Level level, BlockState state, BlockPos pos, BooleanProperty property) {
		if (property == FIRE_POWERED) {
			Direction fireDirection = this.getFireSignalDirection(state);
			return level.getSignal(pos.relative(fireDirection), fireDirection) > 0;
		}
		if (property == ASSEMBLY_POWERED) {
			Direction assemblyDirection = this.getAssemblySignalDirection(state);
			return level.getSignal(pos.relative(assemblyDirection), assemblyDirection) > 0;
		}
		return false;
	}

	private Direction getFireSignalDirection(BlockState state) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		return state.getValue(SIGNALS_REVERSED) ? facing.getOpposite() : facing;
	}

	private Direction getAssemblySignalDirection(BlockState state) {
		return this.getFireSignalDirection(state).getOpposite();
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount) {
			for (KineticBlockEntity kbe : mount.getAllKineticBlockEntities()) {
				kbe.preventSpeedUpdate = 0;
				if (oldState.getBlock() == state.getBlock() && state.hasBlockEntity() == oldState.hasBlockEntity()
					&& this.areStatesKineticallyEquivalent(oldState, state))
					kbe.preventSpeedUpdate = 2;
			}
		}
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags, int count) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)
			mount.tryUpdatingSpeed();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (!context.getLevel().isClientSide
			&& context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CompactCannonMountBlockEntity mount) {
			mount.disassemble();
		}
		return super.onWrenched(state, context);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		if (targetedFace.getAxis().isVertical())
			return originalState.setValue(HORIZONTAL_FACING, originalState.getValue(HORIZONTAL_FACING).getClockWise());
		Direction facing = originalState.getValue(HORIZONTAL_FACING);
		if (targetedFace == facing || targetedFace == facing.getOpposite())
			return originalState.cycle(SIGNALS_REVERSED);
		return originalState.setValue(HORIZONTAL_FACING, targetedFace);
	}

	@Override
	public Class<CompactCannonMountBlockEntity> getBlockEntityClass() {
		return CompactCannonMountBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CompactCannonMountBlockEntity> getBlockEntityType() {
		return MTBlockEntities.COMPACT_CANNON_MOUNT.get();
	}

}
