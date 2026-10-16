package com.cbcfirepowercomponents.mixin.radar;

import com.cbcfirepowercomponents.compat.radar.RadarLegacyWeaponCompat;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.happysg.radar.block.controller.firing.FireControllerBlockEntity", remap = false)
public abstract class FireControllerBlockEntityMixin {
	@Inject(method = "setPoweredInternal", at = @At("RETURN"), require = 1)
	private void cbcfpc$relayLegacyOutput(boolean powered, CallbackInfo ci) {
		RadarLegacyWeaponCompat.relay((BlockEntity) (Object) this, powered);
	}
}
