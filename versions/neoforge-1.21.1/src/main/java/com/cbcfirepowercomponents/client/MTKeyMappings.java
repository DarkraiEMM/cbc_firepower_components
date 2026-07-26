package com.cbcfirepowercomponents.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public final class MTKeyMappings {
	public static final KeyMapping MEASURE_DISTANCE = new KeyMapping(
		"key.cbc_firepower_components.measure_distance",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_R,
		"key.categories.cbc_firepower_components");

	private MTKeyMappings() {}
}
