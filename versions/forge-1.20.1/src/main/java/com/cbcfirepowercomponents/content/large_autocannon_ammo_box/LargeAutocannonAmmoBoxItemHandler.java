package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoType;

class LargeAutocannonAmmoBoxItemHandler implements IItemHandler {
	private final LargeAutocannonAmmoBoxBlockEntity be;

	LargeAutocannonAmmoBoxItemHandler(LargeAutocannonAmmoBoxBlockEntity be) {
		this.be = be;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.isValidSlot(slot) ? this.be.getStoredStackForSlot(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.isValidSlot(slot) || stack.isEmpty() || !this.isItemValid(slot, stack))
			return stack;
		ItemStack stored = this.be.getStoredStackForSlot(slot);
		if (!stored.isEmpty() && !ItemStack.isSameItemSameTags(stored, stack))
			return stack;
		int space = this.getSlotLimit(slot) - this.be.getStoredCount(slot);
		if (space <= 0)
			return stack;
		int inserted = Math.min(space, stack.getCount());
		if (!simulate)
			this.be.insertStoredStack(slot, stack, inserted);
		ItemStack remainder = stack.copy();
		remainder.shrink(inserted);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return this.isValidSlot(slot) ? this.be.extractStoredStack(slot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.isValidSlot(slot) ? LargeAutocannonAmmoBoxBlockEntity.LARGE_AMMO_CAPACITY : 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if (!this.isValidSlot(slot) || stack.isEmpty())
			return false;
		AutocannonAmmoType incomingType = AutocannonAmmoType.of(stack);
		if (incomingType == AutocannonAmmoType.NONE)
			return false;
		AutocannonAmmoType storedType = this.be.getAmmoType();
		return storedType == AutocannonAmmoType.NONE || incomingType == storedType;
	}

	private boolean isValidSlot(int slot) {
		return slot == 0 || slot == 1;
	}
}
