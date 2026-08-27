package com.cbcfirepowercomponents.content.cannon_limiter;

import net.minecraft.world.item.ItemStack;

/** Common limiter storage exposed by both this mod's mounts and CBC's standard mount mixin. */
public interface CannonLimiterMount {
	boolean hasLimiter();

	ItemStack getLimiterStack();

	void installLimiter(ItemStack stack);

	ItemStack removeLimiter();
}
