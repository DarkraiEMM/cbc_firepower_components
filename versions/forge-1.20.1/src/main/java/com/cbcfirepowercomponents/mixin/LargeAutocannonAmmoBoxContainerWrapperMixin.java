package com.cbcfirepowercomponents.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxCapacity;

import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntityContainerWrapper;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.IAutocannonAmmoContainerContainer;

@Mixin(value = AutocannonAmmoContainerBlockEntityContainerWrapper.class, remap = false)
public abstract class LargeAutocannonAmmoBoxContainerWrapperMixin implements IAutocannonAmmoContainerContainer {
	@Shadow
	@Final
	private AutocannonAmmoContainerBlockEntity be;

	@Override
	public int getMainAmmoCapacity() {
		return this.be instanceof LargeAutocannonAmmoBoxBlockEntity largeBox
			? largeBox.getMainAmmoCapacity()
			: LargeAutocannonAmmoBoxCapacity.defaultMainAmmoCapacity(this);
	}

	@Override
	public int getTracerAmmoCapacity() {
		return this.be instanceof LargeAutocannonAmmoBoxBlockEntity largeBox
			? largeBox.getTracerAmmoCapacity()
			: LargeAutocannonAmmoBoxCapacity.defaultTracerAmmoCapacity(this);
	}

	@Override
	public int getTotalCount() {
		return this.be instanceof LargeAutocannonAmmoBoxBlockEntity largeBox
			? largeBox.getTotalCount()
			: this.getMainAmmoStack().getCount() + this.getTracerStack().getCount();
	}
}
