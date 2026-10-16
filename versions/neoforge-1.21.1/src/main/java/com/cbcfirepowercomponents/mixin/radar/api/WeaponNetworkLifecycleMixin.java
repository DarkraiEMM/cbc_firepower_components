package com.cbcfirepowercomponents.mixin.radar.api;

import com.cbcfirepowercomponents.compat.radar.RadarLiveTrackCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity", remap = false)
public abstract class WeaponNetworkLifecycleMixin {
    @Inject(method = "tick()V", at = @At("RETURN"), require = 1)
    private void cbcfpc$clearDetachedNetworkOutput(CallbackInfo ci) {
        RadarLiveTrackCompat.onControllerTick(this);
    }
}
