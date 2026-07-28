package com.cbcfirepowercomponents.mixin.radar;

import com.cbcfirepowercomponents.compat.radar.RadarCompactMountCompat;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.happysg.radar.block.datalink.DataLinkBlockItem", remap = false)
public abstract class DataLinkBlockItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true, remap = false)
	private void cbcfpc$selectCompactMount(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (RadarCompactMountCompat.trySelectCompactMount(context))
			cir.setReturnValue(InteractionResult.SUCCESS);
	}

	@Inject(method = "isCannonMountBE", at = @At("HEAD"), cancellable = true, remap = false)
	private static void cbcfpc$isCompactMount(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
		if (blockEntity instanceof CompactCannonMountBlockEntity)
			cir.setReturnValue(true);
	}
}