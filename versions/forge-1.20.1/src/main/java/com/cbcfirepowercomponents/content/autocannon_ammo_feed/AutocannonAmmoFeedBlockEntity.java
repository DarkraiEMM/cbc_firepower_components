package com.cbcfirepowercomponents.content.autocannon_ammo_feed;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;

public class AutocannonAmmoFeedBlockEntity extends BlockEntity {
	private ItemStack ammo = ItemStack.EMPTY;
	private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new FeedItemHandler());

	public AutocannonAmmoFeedBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.AUTOCANNON_AMMO_FEED.get(), pos, state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return this.itemHandler.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.itemHandler.invalidate();
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
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (!this.ammo.isEmpty())
			tag.put("Ammo", this.ammo.save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.ammo = tag.contains("Ammo") ? ItemStack.of(tag.getCompound("Ammo")) : ItemStack.EMPTY;
	}

	private class FeedItemHandler implements IItemHandler {
		@Override public int getSlots() { return 1; }
		@Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? AutocannonAmmoFeedBlockEntity.this.ammo : ItemStack.EMPTY; }

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || stack.isEmpty() || !(stack.getItem() instanceof AutocannonAmmoItem))
				return stack;
			ItemStack stored = AutocannonAmmoFeedBlockEntity.this.ammo;
			if (!stored.isEmpty() && !ItemStack.isSameItemSameTags(stored, stack))
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
			ItemStack extracted = AutocannonAmmoFeedBlockEntity.this.ammo.copy();
			extracted.setCount(Math.min(amount, AutocannonAmmoFeedBlockEntity.this.ammo.getCount()));
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
