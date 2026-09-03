package com.cbcfirepowercomponents.mixin.radar.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;

import net.minecraft.world.level.block.entity.BlockEntity;

/** Mirrors Radar's actual fire-controller output to the associated API mount. */
@Pseudo
@Mixin(targets = "com.happysg.radar.block.controller.firing.FireControllerBlockEntity", remap = false)
public abstract class FireControllerBlockEntityMixin {
	@Inject(method = "setPoweredInternal", at = @At("TAIL"), require = 0)
	private void cbcfpc$relayOutput(boolean powered, CallbackInfo ci) {
		BlockEntity self = (BlockEntity) (Object) this;
		RadarApiCompat.relayFireController(self.getLevel(), self.getBlockPos(), powered);
	}

	@Inject(method = "onChunkUnloaded", at = @At("HEAD"), require = 0)
	private void cbcfpc$stopOnChunkUnload(CallbackInfo ci) {
		BlockEntity self = (BlockEntity) (Object) this;
		RadarApiCompat.relayFireController(self.getLevel(), self.getBlockPos(), false);
	}
}
