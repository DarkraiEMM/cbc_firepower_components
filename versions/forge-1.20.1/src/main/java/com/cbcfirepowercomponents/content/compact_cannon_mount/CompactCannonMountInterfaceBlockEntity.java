package com.cbcfirepowercomponents.content.compact_cannon_mount;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public abstract class CompactCannonMountInterfaceBlockEntity extends KineticBlockEntity {

	protected final CompactCannonMountBlockEntity parent;
	private double sequencedAngleLimit;

	public CompactCannonMountInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, CompactCannonMountBlockEntity parent) {
		super(type, pos, state);
		this.parent = parent;
		this.setLazyTickRate(3);
		this.sequencedAngleLimit = -1;
	}

	@Override
	public float calculateStressApplied() {
		return this.parent.calculateCannonStressApplied();
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		this.sequencedAngleLimit = -1;

		PitchOrientedContraptionEntity mountedContraption = this.parent.mountedContraption;
		if (mountedContraption != null && Math.signum(previousSpeed) != Math.signum(this.getSpeed()) && previousSpeed != 0) {
			if (!mountedContraption.isStalled())
				this.applyRotationAndAngle();
		}

		if (this.sequenceContext != null && this.sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE) {
			this.sequencedAngleLimit = this.sequenceContext.getEffectiveValue(getTheoreticalSpeed()) * 0.125f;
		}
	}

	public void tryUpdateSpeed() {
		if (this.preventSpeedUpdate > 0)
			return;
		this.warnOfMovement();
		this.clearKineticInformation();
		this.updateSpeed = true;
	}

	public double getSequencedAngleLimit() {
		return this.sequencedAngleLimit;
	}

	public void setSequencedAngleLimit(double value) {
		this.sequencedAngleLimit = value;
	}

	@Override public void sendData() { this.parent.sendData(); }
	@Override public void setChanged() { this.parent.setChanged(); }

	protected abstract void applyRotationAndAngle();

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (this.sequencedAngleLimit >= 0)
			tag.putDouble("SequencedAngleLimit", this.sequencedAngleLimit);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		this.sequencedAngleLimit = tag.contains("SequencedAngleLimit") ? tag.getDouble("SequencedAngleLimit") : -1;
	}

	public static class PitchInterface extends CompactCannonMountInterfaceBlockEntity {
		public PitchInterface(BlockEntityType<?> type, BlockPos pos, BlockState state, CompactCannonMountBlockEntity parent) {
			super(type, pos, state, parent);
		}

		@Override
		public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
			Direction.Axis axis = block.getRotationAxis(state);
			BlockPos pitchInput = BlockPos.ZERO.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
			List<BlockPos> locations = new ArrayList<>();
			locations.add(this.worldPosition.offset(pitchInput));
			locations.add(this.worldPosition.subtract(pitchInput));
			return locations;
		}

		@Override
		protected void applyRotationAndAngle() {
			this.parent.applyRotation();
		}
	}

	public static class YawInterface extends CompactCannonMountInterfaceBlockEntity {
		public YawInterface(BlockEntityType<?> type, BlockPos pos, BlockState state, CompactCannonMountBlockEntity parent) {
			super(type, pos, state, parent);
		}

		@Override
		public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
			List<BlockPos> locations = new ArrayList<>();
			locations.add(this.worldPosition.above());
			locations.add(this.worldPosition.below());
			return locations;
		}

		@Override
		protected void applyRotationAndAngle() {
			this.parent.applyRotation();
		}
	}

}
