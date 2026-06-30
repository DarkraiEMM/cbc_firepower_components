package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;

public class LargeAutocannonAmmoBoxBlockEntity extends AutocannonAmmoContainerBlockEntity {
	public static final int LARGE_AMMO_CAPACITY = 64;
	static final int MAX_SERIALIZED_STACK_COUNT = 99;

	private static final String MAIN_AMMO_COUNT_TAG = "LargeMainAmmoCount";
	private static final String TRACER_AMMO_COUNT_TAG = "LargeTracerAmmoCount";

	private int mainAmmoCount;
	private int tracerAmmoCount;
	private final IItemHandler largeInventory = new LargeAutocannonAmmoBoxItemHandler(this);

	public LargeAutocannonAmmoBoxBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.LARGE_AUTOCANNON_AMMO_BOX.get(), pos, state);
	}

	@Override
	public int getMainAmmoCapacity() {
		return LARGE_AMMO_CAPACITY;
	}

	@Override
	public int getTracerAmmoCapacity() {
		return LARGE_AMMO_CAPACITY;
	}

	@Override
	public int getTotalCount() {
		return this.mainAmmoCount + this.tracerAmmoCount;
	}

	public int getStoredCount(int slot) {
		return slot == 1 ? this.tracerAmmoCount : this.mainAmmoCount;
	}

	ItemStack getStoredStackForSlot(int slot) {
		return slot == 1 ? super.getTracerStack() : super.getMainAmmoStack();
	}

	@Override
	public void setMainAmmoDirect(ItemStack stack) {
		this.setStoredStack(0, stack);
	}

	@Override
	public void setTracersDirect(ItemStack stack) {
		this.setStoredStack(1, stack);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0 || slot == 1)
			this.setStoredStack(slot, stack);
		this.setChanged();
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0 && slot != 1)
			return ItemStack.EMPTY;
		ItemStack extracted = this.extractStoredStack(slot, amount, false);
		if (!extracted.isEmpty())
			this.setChanged();
		return extracted;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot != 0 && slot != 1)
			return ItemStack.EMPTY;
		return this.extractStoredStack(slot, MAX_SERIALIZED_STACK_COUNT, false);
	}

	@Override
	public void clearContent() {
		this.mainAmmoCount = 0;
		this.tracerAmmoCount = 0;
		super.setMainAmmoDirect(ItemStack.EMPTY);
		super.setTracersDirect(ItemStack.EMPTY);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return LargeAutocannonAmmoBoxCapacity.canPlace(this, slot, stack);
	}

	@Override
	public IItemHandler getItemHandler(Direction side) {
		return this.largeInventory;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		this.syncSerializedStacks();
		super.saveAdditional(tag, registries);
		tag.putInt(MAIN_AMMO_COUNT_TAG, this.mainAmmoCount);
		tag.putInt(TRACER_AMMO_COUNT_TAG, this.tracerAmmoCount);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.mainAmmoCount = tag.contains(MAIN_AMMO_COUNT_TAG) ? tag.getInt(MAIN_AMMO_COUNT_TAG) : super.getMainAmmoStack().getCount();
		this.tracerAmmoCount = tag.contains(TRACER_AMMO_COUNT_TAG) ? tag.getInt(TRACER_AMMO_COUNT_TAG) : super.getTracerStack().getCount();
		this.mainAmmoCount = Mth.clamp(this.mainAmmoCount, 0, LARGE_AMMO_CAPACITY);
		this.tracerAmmoCount = Mth.clamp(this.tracerAmmoCount, 0, LARGE_AMMO_CAPACITY);
		this.syncSerializedStacks();
	}

	void insertStoredStack(int slot, ItemStack stack, int amount) {
		if (slot != 0 && slot != 1 || stack.isEmpty() || amount <= 0)
			return;
		int oldCount = this.getStoredCount(slot);
		int newCount = Mth.clamp(oldCount + amount, 0, LARGE_AMMO_CAPACITY);
		this.setStoredStack(slot, stack.copyWithCount(newCount));
		this.setChanged();
	}

	ItemStack extractStoredStack(int slot, int amount, boolean simulate) {
		if (slot != 0 && slot != 1 || amount <= 0)
			return ItemStack.EMPTY;
		ItemStack stored = this.getStoredStackForSlot(slot);
		int storedCount = this.getStoredCount(slot);
		if (stored.isEmpty() || storedCount <= 0)
			return ItemStack.EMPTY;
		int extractedCount = Math.min(Math.min(amount, storedCount), MAX_SERIALIZED_STACK_COUNT);
		ItemStack extracted = stored.copyWithCount(extractedCount);
		if (!simulate) {
			this.setStoredStack(slot, stored.copyWithCount(storedCount - extractedCount));
			this.setChanged();
		}
		return extracted;
	}

	private void setStoredStack(int slot, ItemStack stack) {
		if (slot != 0 && slot != 1)
			return;
		int count = stack.isEmpty() ? 0 : Mth.clamp(stack.getCount(), 0, LARGE_AMMO_CAPACITY);
		ItemStack safeStack = ItemStack.EMPTY;
		if (count > 0)
			safeStack = stack.copyWithCount(Math.min(count, MAX_SERIALIZED_STACK_COUNT));
		if (slot == 1) {
			this.tracerAmmoCount = count;
			super.setTracersDirect(safeStack);
		} else {
			this.mainAmmoCount = count;
			super.setMainAmmoDirect(safeStack);
		}
	}

	private void syncSerializedStacks() {
		this.setStoredStack(0, this.mainAmmoCount <= 0 ? ItemStack.EMPTY : super.getMainAmmoStack().copyWithCount(this.mainAmmoCount));
		this.setStoredStack(1, this.tracerAmmoCount <= 0 ? ItemStack.EMPTY : super.getTracerStack().copyWithCount(this.tracerAmmoCount));
	}
}
