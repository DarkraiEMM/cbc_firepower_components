package com.cbcfirepowercomponents.mixin.radar.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;

import net.minecraft.world.level.block.entity.BlockEntity;

/** Supplies Radar's API pitch handler with the same shaft input used by its CBC handler. */
@Pseudo
@Mixin(targets = "com.happysg.radar.block.controller.pitch.ApiMountPitch", remap = false)
public abstract class ApiMountPitchSpeedMixin {
	@Redirect(method = "tick", at = @At(value = "INVOKE", target =
		"Lcom/happysg/radar/block/controller/pitch/AutoPitchControllerBlockEntity;getSpeed()F"), require = 1)
	private float cbcfpc$useAdjacentShaftInput(@Coerce Object controller) {
		return RadarApiCompat.getOfficialMountInputSpeed((BlockEntity) controller);
	}
}
