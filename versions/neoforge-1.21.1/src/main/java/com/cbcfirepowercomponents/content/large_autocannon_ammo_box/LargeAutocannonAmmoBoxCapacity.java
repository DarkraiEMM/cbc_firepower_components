package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import com.cbcfirepowercomponents.mixin.AutocannonAmmoContainerItemContainerAccessor;
import com.cbcfirepowercomponents.mixin.AutocannonAmmoContainerWrapperAccessor;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoType;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.IAutocannonAmmoContainerContainer;

public final class LargeAutocannonAmmoBoxCapacity {
	private LargeAutocannonAmmoBoxCapacity() {
	}

	public static boolean isLargeCapacityContainer(IAutocannonAmmoContainerContainer container) {
		return container instanceof LargeAutocannonAmmoBoxBlockEntity
			|| container instanceof AutocannonAmmoContainerWrapperAccessor wrapper
				&& wrapper.cbcfpc$getBlockEntity() instanceof LargeAutocannonAmmoBoxBlockEntity
			|| container instanceof AutocannonAmmoContainerItemContainerAccessor itemContainer
				&& itemContainer.cbcfpc$getStack().getItem() instanceof LargeAutocannonAmmoBoxItem;
	}

	public static int getStoredCount(IAutocannonAmmoContainerContainer container, int slot) {
		if (container instanceof LargeAutocannonAmmoBoxBlockEntity largeBox)
			return largeBox.getStoredCount(slot);
		if (container instanceof AutocannonAmmoContainerWrapperAccessor wrapper
			&& wrapper.cbcfpc$getBlockEntity() instanceof LargeAutocannonAmmoBoxBlockEntity largeBox)
			return largeBox.getStoredCount(slot);
		return container.getItem(slot).getCount();
	}

	public static int getSlotCapacity(IAutocannonAmmoContainerContainer container, int slot) {
		if (isLargeCapacityContainer(container))
			return LargeAutocannonAmmoBoxBlockEntity.LARGE_AMMO_CAPACITY;
		return slot == 1 ? container.getTracerAmmoCapacity() : container.getMainAmmoCapacity();
	}

	public static boolean canPlace(IAutocannonAmmoContainerContainer container, int slot, ItemStack stack) {
		if ((slot != 0 && slot != 1) || stack.isEmpty())
			return false;
		AutocannonAmmoType incomingType = AutocannonAmmoType.of(stack);
		if (incomingType == AutocannonAmmoType.NONE)
			return false;
		AutocannonAmmoType storedType = container.getAmmoType();
		if (storedType != AutocannonAmmoType.NONE && incomingType != storedType)
			return false;
		return getStoredCount(container, slot) < getSlotCapacity(container, slot);
	}

	public static int getMaxStackSize(IAutocannonAmmoContainerContainer container, int slot, ItemStack stack) {
		if ((slot != 0 && slot != 1) || stack.isEmpty())
			return 0;
		return getSlotCapacity(container, slot);
	}

	public static int defaultMainAmmoCapacity(IAutocannonAmmoContainerContainer container) {
		int space = Math.max(0, container.getAmmoType().getCapacity() - container.getTotalCount());
		ItemStack stack = container.getMainAmmoStack();
		return Math.min(stack.getCount() + space, stack.getMaxStackSize());
	}

	public static int defaultTracerAmmoCapacity(IAutocannonAmmoContainerContainer container) {
		int space = Math.max(0, container.getAmmoType().getCapacity() - container.getTotalCount());
		ItemStack stack = container.getTracerStack();
		return Math.min(stack.getCount() + space, stack.getMaxStackSize());
	}
}