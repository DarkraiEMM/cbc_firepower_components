package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;

@Mixin(value = ControlPitchContraption.class, remap = false)
public interface ControlPitchContraptionCompatMixin {
	default void onRecoil(Vec3 recoil, AbstractContraptionEntity entity) {
	}
}
