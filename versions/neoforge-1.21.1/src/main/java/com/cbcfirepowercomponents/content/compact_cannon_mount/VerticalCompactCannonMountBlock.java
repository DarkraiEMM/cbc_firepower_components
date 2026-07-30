package com.cbcfirepowercomponents.content.compact_cannon_mount;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A compact, trunnion-style cannon mount that places the cannon directly above
 * or below the controller. Unlike the vertical autocannon mount, this variant
 * accepts every mounted cannon type supported by the regular compact mount.
 */
public class VerticalCompactCannonMountBlock extends CompactCannonMountBlock {

	public VerticalCompactCannonMountBlock(Properties properties) {
		super(properties);
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
