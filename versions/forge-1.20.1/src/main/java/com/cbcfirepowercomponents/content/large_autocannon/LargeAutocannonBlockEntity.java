package com.cbcfirepowercomponents.content.large_autocannon;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public class LargeAutocannonBlockEntity extends AutocannonBlockEntity implements TwinLargeAutocannonRecoilSource {
	private final TwinLargeAutocannonRecoil twinRecoil = new TwinLargeAutocannonRecoil();

	public LargeAutocannonBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.LARGE_AUTOCANNON.get(), pos, state);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (this.level != null && !this.level.isClientSide) {
			LargeAutocannonBarrelBlock.refreshConnectionState(this.level, this.worldPosition, true);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.incrementTwinAnimationTicks();
	}

	@Override
	public void tickFromContraption(Level level, PitchOrientedContraptionEntity entity, BlockPos localPos) {
		super.tickFromContraption(level, entity, localPos);
		this.incrementTwinAnimationTicks();
		if (!level.isClientSide)
			return;

		if (entity.getContraption().getBlockEntityClientSide(localPos) instanceof TwinLargeAutocannonRecoilSource clientPart
			&& clientPart != this) {
			clientPart.incrementTwinAnimationTicks();
		}
	}

	@Override
	public void handleTwinFiring(boolean rightBarrel, boolean muzzleBrake) {
		this.twinRecoil.handleFiring(rightBarrel, muzzleBrake);
	}

	@Override
	public float getTwinAnimateOffset(boolean rightBarrel, float partialTicks) {
		return this.twinRecoil.getOffset(rightBarrel, partialTicks);
	}

	@Override
	public void incrementTwinAnimationTicks() {
		this.twinRecoil.tick();
	}
}
