package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntityContainerWrapper;

@Mixin(value = AutocannonAmmoContainerBlockEntityContainerWrapper.class, remap = false)
public interface AutocannonAmmoContainerWrapperAccessor {
	@Accessor("be")
	AutocannonAmmoContainerBlockEntity cbcfpc$getBlockEntity();
}