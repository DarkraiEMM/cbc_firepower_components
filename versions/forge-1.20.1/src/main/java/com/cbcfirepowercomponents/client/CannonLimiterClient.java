package com.cbcfirepowercomponents.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class CannonLimiterClient {
	public static void openConfig(InteractionHand hand, ItemStack stack) {
		Minecraft.getInstance().setScreen(new CannonLimiterScreen(hand, stack));
	}
}
