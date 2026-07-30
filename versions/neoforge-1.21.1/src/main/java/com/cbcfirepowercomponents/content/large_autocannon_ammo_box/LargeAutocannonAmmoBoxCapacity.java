package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import com.cbcfirepowercomponents.mixin.AutocannonAmmoContainerItemContainerAccessor;
import com.cbcfirepowercomponents.mixin.AutocannonAmmoContainerWrapperAccessor;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlockEntity;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;
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

	public static boolean isManagedContainer(IAutocannonAmmoContainerContainer container) {
		return isLargeCapacityContainer(container) || container instanceof AutocannonAmmoFeedBlockEntity;
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
		if (!(stack.getItem() instanceof AutocannonAmmoItem incomingAmmo)
			|| incomingAmmo.isTracer(stack) != (slot == 1))
			return false;
		AutocannonAmmoType storedType = container.getAmmoType();
		if (storedType != AutocannonAmmoType.NONE && incomingType != storedType)
			return false;
		ItemStack sameSlot = container.getItem(slot);
		if (!sameSlot.isEmpty() && !ItemStack.isSameItemSameComponents(sameSlot, stack))
			return false;
		ItemStack pairedSlot = container.getItem(slot == 0 ? 1 : 0);
		if (!pairedSlot.isEmpty() && !isSameAmmoIgnoringTracer(pairedSlot, stack))
			return false;
		return getStoredCount(container, slot) < getSlotCapacity(container, slot);
	}

	private static boolean isSameAmmoIgnoringTracer(ItemStack first, ItemStack second) {
		if (!(first.getItem() instanceof AutocannonAmmoItem firstAmmo)
			|| !(second.getItem() instanceof AutocannonAmmoItem secondAmmo))
			return false;
		ItemStack normalizedFirst = first.copyWithCount(1);
		ItemStack normalizedSecond = second.copyWithCount(1);
		firstAmmo.setTracer(normalizedFirst, false);
		secondAmmo.setTracer(normalizedSecond, false);
		return ItemStack.isSameItemSameComponents(normalizedFirst, normalizedSecond);
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
