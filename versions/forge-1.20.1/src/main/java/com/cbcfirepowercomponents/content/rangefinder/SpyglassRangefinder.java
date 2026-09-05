package com.cbcfirepowercomponents.content.rangefinder;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;

public final class SpyglassRangefinder {
	public static final double MAX_RANGE = 512.0;

	private SpyglassRangefinder() {}

	public static void measure(ServerPlayer player) {
		if (!player.isUsingItem() || !player.getUseItem().is(Items.SPYGLASS))
			return;

		HitResult hit = ProjectileUtil.getHitResultOnViewVector(player,
			entity -> !entity.isSpectator() && entity.isPickable(), MAX_RANGE);
		if (hit.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(Component.translatable(
				"item.cbc_firepower_components.spyglass_rangefinder.out_of_range", (int) MAX_RANGE), true);
			return;
		}

		player.displayClientMessage(Component.translatable(
			"item.cbc_firepower_components.spyglass_rangefinder.distance",
			String.format(Locale.ROOT, "%.1f", player.getEyePosition().distanceTo(hit.getLocation()))), true);
	}
}
