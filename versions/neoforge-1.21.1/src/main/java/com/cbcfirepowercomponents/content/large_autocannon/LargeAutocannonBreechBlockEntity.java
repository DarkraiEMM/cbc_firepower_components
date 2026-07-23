package com.cbcfirepowercomponents.content.large_autocannon;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlockEntity;

public class LargeAutocannonBreechBlockEntity extends AutocannonBreechBlockEntity {
	public LargeAutocannonBreechBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.LARGE_AUTOCANNON_BREECH.get(), pos, state);
	}
	@Override
	public void initialize() {
		super.initialize();
		if (this.level != null && !this.level.isClientSide) {
			LargeAutocannonBarrelBlock.refreshConnectionState(this.level, this.worldPosition, true);
		}
	}
}
