package com.cbcfirepowercomponents.content.automatic_cannon_controller;

import net.minecraft.core.BlockPos;

/**
 * A cannon mount that can accept the controller's virtual firing signal
 * without replacing its physical redstone input.
 */
public interface AutomaticFireMount {
	BlockPos getAutomaticFireMountPos();

	void setAutomaticFirePowered(boolean powered, int firePower);

	default int getAutomaticFireIntervalTicks() {
		return 0;
	}
}
