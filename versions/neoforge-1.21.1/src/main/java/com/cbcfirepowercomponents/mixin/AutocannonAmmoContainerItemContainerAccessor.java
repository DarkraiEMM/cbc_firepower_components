package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItemContainer;

@Mixin(value = AutocannonAmmoContainerItemContainer.class, remap = false)
public interface AutocannonAmmoContainerItemContainerAccessor {
	@Accessor("stack")
	ItemStack cbcfpc$getStack();
}