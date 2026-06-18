package com.cbcfirepowercomponents.content.cannon_magazine_loader;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlockItem;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

public class CannonMagazineLoaderBlockEntity extends BlockEntity {
	private static final int PROJECTILE_SLOTS = 3;
	private static final int CARTRIDGE_SLOTS = 3;
	private static final int SLOT_COUNT = PROJECTILE_SLOTS + CARTRIDGE_SLOTS;

	private final ItemStack[] items = new ItemStack[SLOT_COUNT];
	private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new MagazineItemHandler());

	public CannonMagazineLoaderBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), pos, state);
		for (int i = 0; i < SLOT_COUNT; ++i)
			this.items[i] = ItemStack.EMPTY;
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

	public static void tick(Level level, BlockPos pos, BlockState state, CannonMagazineLoaderBlockEntity loader) {
		for (Direction direction : Direction.values()) {
			BlockEntity be = level.getBlockEntity(pos.relative(direction));
			if (!(be instanceof CompactCannonMountBlockEntity mount))
				continue;
			if (loader.tryLoad(mount))
				return;
		}
	}

	private boolean tryLoad(CompactCannonMountBlockEntity mount) {
		for (int slot = 0; slot < SLOT_COUNT; ++slot) {
			ItemStack stored = this.items[slot];
			if (stored.isEmpty())
				continue;
			if (canApplyFuze(stored))
				continue;
			ItemStack remainder = mount.insertCannonMagazineAmmo(stored, false);
			if (ItemStack.matches(remainder, stored) && remainder.getCount() == stored.getCount())
				continue;
			this.items[slot] = remainder;
			this.setChanged();
			return true;
		}
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ListTag list = new ListTag();
		for (int slot = 0; slot < SLOT_COUNT; ++slot) {
			if (this.items[slot].isEmpty())
				continue;
			CompoundTag itemTag = new CompoundTag();
			itemTag.putByte("Slot", (byte) slot);
			this.items[slot].save(itemTag);
			list.add(itemTag);
		}
		tag.put("Items", list);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		for (int i = 0; i < SLOT_COUNT; ++i)
			this.items[i] = ItemStack.EMPTY;
		ListTag list = tag.getList("Items", 10);
		for (int i = 0; i < list.size(); ++i) {
			CompoundTag itemTag = list.getCompound(i);
			int slot = itemTag.getByte("Slot") & 255;
			if (slot < SLOT_COUNT)
				this.items[slot] = ItemStack.of(itemTag);
		}
	}

	private static boolean isProjectileSlot(int slot) {
		return slot >= 0 && slot < PROJECTILE_SLOTS;
	}

	private static boolean isCartridgeSlot(int slot) {
		return slot >= PROJECTILE_SLOTS && slot < SLOT_COUNT;
	}

	private static boolean isValidForSlot(int slot, ItemStack stack) {
		return isProjectileSlot(slot) && stack.getItem() instanceof ProjectileBlockItem
			|| isCartridgeSlot(slot) && stack.getItem() instanceof BigCartridgeBlockItem;
	}

	private ItemStack insertFuze(ItemStack stack, boolean simulate) {
		if (!isFuze(stack))
			return stack;
		for (int slot = 0; slot < PROJECTILE_SLOTS; ++slot) {
			ItemStack projectile = this.items[slot];
			if (!canApplyFuze(projectile))
				continue;
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			if (!simulate) {
				ItemStack fusedProjectile = projectile.copy();
				applyFuze(fusedProjectile, stack);
				this.items[slot] = fusedProjectile;
				this.setChanged();
			}
			return remainder;
		}
		return stack;
	}

	private boolean canInsertFuze(ItemStack stack) {
		if (!isFuze(stack))
			return false;
		for (int slot = 0; slot < PROJECTILE_SLOTS; ++slot)
			if (canApplyFuze(this.items[slot]))
				return true;
		return false;
	}

	private static boolean isFuze(ItemStack stack) {
		return stack.getItem() instanceof FuzeItem;
	}

	private static boolean canApplyFuze(ItemStack projectile) {
		return projectile.getItem() instanceof BlockItem blockItem
			&& blockItem.getBlock() instanceof FuzedProjectileBlock
			&& FuzedProjectileBlock.getFuzeFromItemStack(projectile).isEmpty();
	}

	private static void applyFuze(ItemStack projectile, ItemStack fuze) {
		ItemStack storedFuze = fuze.copy();
		storedFuze.setCount(1);
		CompoundTag blockEntityTag = projectile.getOrCreateTagElement("BlockEntityTag");
		blockEntityTag.put("Fuze", storedFuze.save(new CompoundTag()));
	}

	private class MagazineItemHandler implements IItemHandler {
		@Override public int getSlots() { return SLOT_COUNT; }
		@Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < SLOT_COUNT ? CannonMagazineLoaderBlockEntity.this.items[slot] : ItemStack.EMPTY; }

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (isFuze(stack))
				return CannonMagazineLoaderBlockEntity.this.insertFuze(stack, simulate);
			if (slot < 0 || slot >= SLOT_COUNT || stack.isEmpty() || !isValidForSlot(slot, stack))
				return stack;
			ItemStack stored = CannonMagazineLoaderBlockEntity.this.items[slot];
			if (!stored.isEmpty())
				return stack;
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			if (!simulate) {
				CannonMagazineLoaderBlockEntity.this.items[slot] = stack.copy();
				CannonMagazineLoaderBlockEntity.this.items[slot].setCount(1);
				CannonMagazineLoaderBlockEntity.this.setChanged();
			}
			return remainder;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot < 0 || slot >= SLOT_COUNT || amount <= 0 || CannonMagazineLoaderBlockEntity.this.items[slot].isEmpty())
				return ItemStack.EMPTY;
			ItemStack extracted = CannonMagazineLoaderBlockEntity.this.items[slot].copy();
			if (!simulate) {
				CannonMagazineLoaderBlockEntity.this.items[slot] = ItemStack.EMPTY;
				CannonMagazineLoaderBlockEntity.this.setChanged();
			}
			return extracted;
		}

		@Override public int getSlotLimit(int slot) { return 1; }
		@Override public boolean isItemValid(int slot, ItemStack stack) {
			return isFuze(stack) ? CannonMagazineLoaderBlockEntity.this.canInsertFuze(stack) : isValidForSlot(slot, stack);
		}
	}
}
