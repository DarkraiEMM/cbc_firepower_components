package com.cbcfirepowercomponents.mixin.radar.api;

import java.util.Set;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** EA 3 contact discovery still checks only CBC contexts, unlike explicit API links. */
@Pseudo
@Mixin(targets = "com.happysg.radar.block.behavior.networks.WeaponNetworkRuntime", remap = false)
public abstract class WeaponNetworkRuntimeMixin {
	@Shadow @Final private ServerLevel level;
	@Inject(method = "addDirectMount", at = @At("HEAD"), cancellable = true, require = 1)
	private void cbcfpc$discoverPublicCompactMount(Set<BlockPos> mounts, BlockPos pos, CallbackInfo ci) {
		if (pos != null && level.hasChunkAt(pos)
			&& level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount && !mount.isRemoved()) {
			mounts.add(pos.immutable());
			ci.cancel();
		}
	}
}
