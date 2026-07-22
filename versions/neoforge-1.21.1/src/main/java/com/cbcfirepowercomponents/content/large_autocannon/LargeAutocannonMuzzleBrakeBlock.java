package com.cbcfirepowercomponents.content.large_autocannon;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

public class LargeAutocannonMuzzleBrakeBlock extends LargeAutocannonBarrelBlock {
	public LargeAutocannonMuzzleBrakeBlock(Properties properties, AutocannonMaterial material) {
		super(properties, material);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) return null;
		state = state.setValue(FACING, context.getClickedFace());
		return this.updateConnections(state, context.getLevel(), context.getClickedPos());
	}

	@Override
	public boolean canConnectToSide(BlockState state, Direction direction) {
		return direction == this.getFacing(state).getOpposite();
	}
}