package com.cbcfirepowercomponents.content.compact_cannon_mount.input;

import java.util.List;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonMunitionBlock;

public final class MountedWeaponInputStrategies {
	private static final List<MountedWeaponInputStrategy> STRATEGIES = List.of(
		new NormalAutocannonStrategy(),
		new ItemCannonStrategy(),
		new BigCannonStrategy(),
		new MountedItemHandlerStrategy()
	);

	private MountedWeaponInputStrategies() {}

	public static boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
		if (stack.isEmpty())
			return false;
		for (MountedWeaponInputStrategy strategy : STRATEGIES) {
			if (strategy.canInsert(context, stack))
				return true;
		}
		return false;
	}

	public static ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return stack;
		for (MountedWeaponInputStrategy strategy : STRATEGIES) {
			if (!strategy.canInsert(context, stack))
				continue;
			ItemStack result = strategy.insert(context, stack, simulate);
			if (!sameStackAndCount(result, stack))
				return result;
		}
		return stack;
	}

	private static boolean sameStackAndCount(ItemStack first, ItemStack second) {
		return ItemStack.matches(first, second) && first.getCount() == second.getCount();
	}

	private static final class NormalAutocannonStrategy implements MountedWeaponInputStrategy {
		@Override
		public boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
			if (!(stack.getItem() instanceof AutocannonAmmoItem)
				&& !(stack.getItem() instanceof AutocannonAmmoContainerItem))
				return false;
			AbstractAutocannonBreechBlockEntity breech = findAutocannonBreech(context.cannon());
			if (breech == null)
				return false;
			if (stack.getItem() instanceof AutocannonAmmoItem)
				return !breech.isInputFull();
			ItemStack magazine = breech.getMagazine();
			return !(magazine.getItem() instanceof AutocannonAmmoContainerItem)
				|| AutocannonAmmoContainerItem.getTotalAmmoCount(magazine) <= 0;
		}

		@Override
		public ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			AbstractAutocannonBreechBlockEntity breech = findAutocannonBreech(context.cannon());
			if (breech == null)
				return stack;
			if (stack.getItem() instanceof AutocannonAmmoContainerItem)
				return insertAutocannonAmmoContainer(breech, stack, simulate);
			if (stack.getItem() instanceof AutocannonAmmoItem)
				return insertLooseAutocannonAmmo(breech, stack, simulate);
			return stack;
		}

		private static ItemStack insertAutocannonAmmoContainer(AbstractAutocannonBreechBlockEntity breech, ItemStack stack,
															  boolean simulate) {
			ItemStack oldContainer = breech.getMagazine();
			if (oldContainer.getItem() instanceof AutocannonAmmoContainerItem
				&& AutocannonAmmoContainerItem.getTotalAmmoCount(oldContainer) > 0)
				return stack;
			if (simulate)
				return ItemStack.EMPTY;
			ItemStack inserted = stack.copy();
			inserted.setCount(1);
			LargeAutocannonAmmoBoxItem.sanitizeForCbcMagazine(inserted);
			breech.setMagazine(inserted);
			breech.setChanged();
			return oldContainer.isEmpty() ? ItemStack.EMPTY : oldContainer.copy();
		}

		private static ItemStack insertLooseAutocannonAmmo(AbstractAutocannonBreechBlockEntity breech, ItemStack stack,
														  boolean simulate) {
			if (breech.isInputFull())
				return stack;
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			if (!simulate) {
				ItemStack inserted = stack.copy();
				inserted.setCount(1);
				breech.getInputBuffer().add(inserted);
				breech.setChanged();
			}
			return remainder;
		}
	}

	private static final class ItemCannonStrategy implements MountedWeaponInputStrategy {
		@Override
		public boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
			return context.itemCannon() != null;
		}

		@Override
		public ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			ItemCannon itemCannon = context.itemCannon();
			return itemCannon == null ? stack : itemCannon.insertItemIntoCannon(stack, simulate);
		}
	}

	private static final class MountedItemHandlerStrategy implements MountedWeaponInputStrategy {
		@Override
		public boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
			IItemHandler handler = context.itemHandler();
			if (handler == null)
				return false;
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				if (handler.isItemValid(slot, stack))
					return true;
				if (!sameStackAndCount(handler.insertItem(slot, stack, true), stack))
					return true;
			}
			return false;
		}

		@Override
		public ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			IItemHandler handler = context.itemHandler();
			if (handler == null)
				return stack;
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				ItemStack result = handler.insertItem(slot, stack, simulate);
				if (!sameStackAndCount(result, stack))
					return result;
			}
			return stack;
		}
	}

	private static final class BigCannonStrategy implements MountedWeaponInputStrategy {
		@Override
		public boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
			if (!(context.cannon() instanceof MountedBigCannonContraption))
				return false;
			return stack.getItem() instanceof BlockItem blockItem
				&& blockItem.getBlock() instanceof BigCannonMunitionBlock;
		}

		@Override
		public ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			if (!(context.cannon() instanceof MountedBigCannonContraption bigCannon))
				return stack;
			return CannonMountPoint.bigCannonInsert(stack, simulate, bigCannon, context.entity());
		}
	}

	@Nullable
	private static AbstractAutocannonBreechBlockEntity findAutocannonBreech(AbstractMountedCannonContraption cannon) {
		BlockEntity startBlockEntity = cannon.presentBlockEntities.get(cannon.getStartPos());
		if (startBlockEntity instanceof AbstractAutocannonBreechBlockEntity breech)
			return breech;
		for (BlockEntity blockEntity : cannon.presentBlockEntities.values()) {
			if (blockEntity instanceof AbstractAutocannonBreechBlockEntity breech)
				return breech;
		}
		return null;
	}
}
