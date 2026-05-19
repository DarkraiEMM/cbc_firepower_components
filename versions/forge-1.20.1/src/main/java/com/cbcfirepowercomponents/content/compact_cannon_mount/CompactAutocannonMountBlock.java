package com.cbcfirepowercomponents.content.compact_cannon_mount;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CompactAutocannonMountBlock extends CompactCannonMountBlock {

	public CompactAutocannonMountBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isAutocannonOnly() {
		return true;
	}

	@Override
	public Direction getCannonSide(BlockState state) {
		return state.getValue(VERTICAL_DIRECTION).getOpposite();
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		if (targetedFace.getAxis().isVertical())
			return originalState.setValue(VERTICAL_DIRECTION, originalState.getValue(VERTICAL_DIRECTION).getOpposite());
		return super.getRotatedBlockState(originalState, targetedFace);
	}

}
