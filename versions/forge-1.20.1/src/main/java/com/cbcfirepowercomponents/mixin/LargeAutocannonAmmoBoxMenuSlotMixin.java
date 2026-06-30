package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxCapacity;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerMenuSlot;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.IAutocannonAmmoContainerContainer;

@Mixin(value = AutocannonAmmoContainerMenuSlot.class, remap = false)
public abstract class LargeAutocannonAmmoBoxMenuSlotMixin extends Slot {
	@Shadow
	@Final
	private IAutocannonAmmoContainerContainer ammoContainer;

	@Shadow
	@Final
	private boolean isCreative;

	private LargeAutocannonAmmoBoxMenuSlotMixin(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void cbcfpc$largeAmmoBoxMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!LargeAutocannonAmmoBoxCapacity.isLargeCapacityContainer(this.ammoContainer))
			return;
		cir.setReturnValue(!this.isCreative
			&& LargeAutocannonAmmoBoxCapacity.canPlace(this.ammoContainer, this.getContainerSlot(), stack));
	}

	@Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
	private void cbcfpc$largeAmmoBoxMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!LargeAutocannonAmmoBoxCapacity.isLargeCapacityContainer(this.ammoContainer))
			return;
		cir.setReturnValue(this.isCreative ? 1
			: LargeAutocannonAmmoBoxCapacity.getMaxStackSize(this.ammoContainer, this.getContainerSlot(), stack));
	}
}