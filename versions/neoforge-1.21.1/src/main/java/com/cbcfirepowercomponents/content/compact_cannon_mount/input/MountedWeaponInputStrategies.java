package com.cbcfirepowercomponents.content.compact_cannon_mount.input;

import java.lang.reflect.Method;
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
		new CBCMoreShellsStrategy(),
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

	/**
	 * CBC Military Supplement uses its own mounted-cannon classes and arm-loading
	 * entry points. Reflection keeps that optional dependency out of the base mod.
	 */
	private static final class CBCMoreShellsStrategy implements MountedWeaponInputStrategy {
		private static final List<CBCMoreShellsLoader> LOADERS = List.of(
			new CBCMoreShellsLoader(
				"com.cainiao1053.cbcmoreshells.cannon_control.contraption.MountedDualCannonContraption",
				"com.cainiao1053.cbcmoreshells.cannons.dual_cannon.breeches.quick_firing_breech.DualCannonMountPoint",
				"dualCannonInsert", "dualCannonInsertCustomized"),
			new CBCMoreShellsLoader(
				"com.cainiao1053.cbcmoreshells.cannon_control.contraption.MountedProjectileRackContraption",
				"com.cainiao1053.cbcmoreshells.cannons.projectile_rack.breeches.quick_firing_breech.ProjectileRackCannonMountPoint",
				"projectileRackInsert"),
			new CBCMoreShellsLoader(
				"com.cainiao1053.cbcmoreshells.cannon_control.contraption.MountedTorpedoTubeContraption",
				"com.cainiao1053.cbcmoreshells.cannons.torpedo_tube.breeches.quick_firing_breech.TorpedoCannonMountPoint",
				"torpedoTubeInsert", "torpedoTubeInsertCustom")
		);

		@Override
		public boolean canInsert(MountedWeaponInputContext context, ItemStack stack) {
			return !sameStackAndCount(this.insert(context, stack, true), stack);
		}

		@Override
		public ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			for (CBCMoreShellsLoader loader : LOADERS) {
				ItemStack result = loader.insert(context, stack, simulate);
				if (!sameStackAndCount(result, stack))
					return result;
			}
			return stack;
		}
	}

	private static final class CBCMoreShellsLoader {
		private final String contraptionClassName;
		private final String mountPointClassName;
		private final String[] methodNames;
		@Nullable private Class<?> contraptionClass;
		@Nullable private Method[] methods;
		private boolean resolved;

		private CBCMoreShellsLoader(String contraptionClassName, String mountPointClassName, String... methodNames) {
			this.contraptionClassName = contraptionClassName;
			this.mountPointClassName = mountPointClassName;
			this.methodNames = methodNames;
		}

		private ItemStack insert(MountedWeaponInputContext context, ItemStack stack, boolean simulate) {
			this.resolve();
			if (this.contraptionClass == null || this.methods == null || !this.contraptionClass.isInstance(context.cannon()))
				return stack;
			try {
				for (Method method : this.methods) {
					Object result = method.invoke(null, stack, simulate, context.cannon(), context.entity());
					if (result instanceof ItemStack remainder && !sameStackAndCount(remainder, stack))
						return remainder;
				}
			} catch (ReflectiveOperationException | LinkageError ignored) {
				// CBCMS is optional; failed compatibility calls leave the stack untouched.
			}
			return stack;
		}

		private void resolve() {
			if (this.resolved)
				return;
			this.resolved = true;
			try {
				this.contraptionClass = Class.forName(this.contraptionClassName);
				Class<?> mountPointClass = Class.forName(this.mountPointClassName);
				this.methods = new Method[this.methodNames.length];
				for (int i = 0; i < this.methodNames.length; ++i)
					this.methods[i] = mountPointClass.getMethod(this.methodNames[i], ItemStack.class, boolean.class,
						this.contraptionClass, rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity.class);
			} catch (ReflectiveOperationException | LinkageError ignored) {
				this.contraptionClass = null;
				this.methods = null;
			}
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
