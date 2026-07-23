package com.cbcfirepowercomponents.content.large_autocannon;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.autocannon.AutocannonBlockEntity;

public class LargeAutocannonBlockEntity extends AutocannonBlockEntity {
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
}
