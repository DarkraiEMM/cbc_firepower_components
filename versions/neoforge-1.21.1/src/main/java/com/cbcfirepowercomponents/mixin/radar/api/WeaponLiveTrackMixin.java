package com.cbcfirepowercomponents.mixin.radar.api;

import com.cbcfirepowercomponents.compat.radar.RadarLiveTrackCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.happysg.radar.block.behavior.networks.WeaponFiringControl", remap = false)
public abstract class WeaponLiveTrackMixin {
    @Shadow public abstract void stopWeaponOutput();

    @Inject(method = "tickGenericWeapon()V", at = @At("HEAD"), cancellable = true, require = 1)
    private void cbcfpc$refreshSelectedContact(CallbackInfo ci) {
        if (!RadarLiveTrackCompat.refresh(this)) {
            stopWeaponOutput();
            ci.cancel();
        }
    }
}
