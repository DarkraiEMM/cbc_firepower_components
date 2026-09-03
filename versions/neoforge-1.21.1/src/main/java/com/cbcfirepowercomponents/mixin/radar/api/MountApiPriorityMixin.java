package com.cbcfirepowercomponents.mixin.radar.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Keeps Radar's official add-on mount handler authoritative when the separate
 * weapon-context bridge also makes the same block recognizable as a CBC mount.
 */
@Pseudo
@Mixin(targets = {
	"com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity",
	"com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity"
}, remap = false)
public abstract class MountApiPriorityMixin {
	@Inject(method = "resolveKineticMount", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$preferOfficialApiOverStructuralMount(CallbackInfoReturnable<Object> cir) {
		Object absent = RadarApiCompat.deferStructuralKineticSelectionForOfficialMount(
			(net.minecraft.world.level.block.entity.BlockEntity) (Object) this);
		if (absent != null)
			cir.setReturnValue(absent);
	}

	@Redirect(method = "refreshMountCache", at = @At(value = "INVOKE", target =
		"Lcom/happysg/radar/compat/cbc/CannonMountContext;resolveEndpoint(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/happysg/radar/compat/cbc/CannonMountContext;"), require = 1)
	@Coerce
	private Object cbcfpc$deferCompactMountToOfficialApi(Level level, BlockPos mountPos) {
		return RadarApiCompat.resolveCbcEndpointForMountControl(level, mountPos);
	}
}
