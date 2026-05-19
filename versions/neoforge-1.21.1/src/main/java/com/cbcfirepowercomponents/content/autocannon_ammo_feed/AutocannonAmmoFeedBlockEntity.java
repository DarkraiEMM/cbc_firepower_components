package com.cbcfirepowercomponents.content.autocannon_ammo_feed;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;

public class AutocannonAmmoFeedBlockEntity extends BlockEntity {
	private ItemStack ammo = ItemStack.EMPTY;
	private final IItemHandler itemHandler = new FeedItemHandler();

	public AutocannonAmmoFeedBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.AUTOCANNON_AMMO_FEED.get(), pos, state);
	}

	@Nullable
	public IItemHandler getItemHandler(Direction side) {
		return this.itemHandler;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AutocannonAmmoFeedBlockEntity feed) {
		if (feed.ammo.isEmpty())
			return;
		for (Direction direction : Direction.values()) {
			BlockEntity be = level.getBlockEntity(pos.relative(direction));
			if (!(be instanceof CompactCannonMountBlockEntity mount))
				continue;
			ItemStack remainder = mount.insertAutocannonFeedAmmo(feed.ammo, false);
			if (!ItemStack.matches(remainder, feed.ammo) || remainder.getCount() != feed.ammo.getCount()) {
				feed.ammo = remainder;
				feed.setChanged();
				return;
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!this.ammo.isEmpty())
			tag.put("Ammo", this.ammo.saveOptional(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.ammo = tag.contains("Ammo") ? ItemStack.parseOptional(registries, tag.getCompound("Ammo")) : ItemStack.EMPTY;
	}

	private class FeedItemHandler implements IItemHandler {
		@Override public int getSlots() { return 1; }
		@Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? AutocannonAmmoFeedBlockEntity.this.ammo : ItemStack.EMPTY; }

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || stack.isEmpty() || !(stack.getItem() instanceof AutocannonAmmoItem))
				return stack;
			ItemStack stored = AutocannonAmmoFeedBlockEntity.this.ammo;
			if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack))
				return stack;
			int limit = Math.min(stack.getMaxStackSize(), 64);
			int space = limit - stored.getCount();
			if (space <= 0)
				return stack;
			int inserted = Math.min(space, stack.getCount());
			ItemStack remainder = stack.copy();
			remainder.shrink(inserted);
			if (!simulate) {
				if (stored.isEmpty()) {
					AutocannonAmmoFeedBlockEntity.this.ammo = stack.copy();
					AutocannonAmmoFeedBlockEntity.this.ammo.setCount(inserted);
				} else {
					stored.grow(inserted);
				}
				AutocannonAmmoFeedBlockEntity.this.setChanged();
			}
			return remainder;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot != 0 || amount <= 0 || AutocannonAmmoFeedBlockEntity.this.ammo.isEmpty())
				return ItemStack.EMPTY;
			ItemStack extracted = AutocannonAmmoFeedBlockEntity.this.ammo.copyWithCount(Math.min(amount, AutocannonAmmoFeedBlockEntity.this.ammo.getCount()));
			if (!simulate) {
				AutocannonAmmoFeedBlockEntity.this.ammo.shrink(extracted.getCount());
				if (AutocannonAmmoFeedBlockEntity.this.ammo.isEmpty())
					AutocannonAmmoFeedBlockEntity.this.ammo = ItemStack.EMPTY;
				AutocannonAmmoFeedBlockEntity.this.setChanged();
			}
			return extracted;
		}

		@Override public int getSlotLimit(int slot) { return 64; }
		@Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof AutocannonAmmoItem; }
	}
}
