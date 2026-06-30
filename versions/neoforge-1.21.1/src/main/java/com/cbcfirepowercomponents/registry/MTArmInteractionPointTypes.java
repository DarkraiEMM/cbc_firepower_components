package com.cbcfirepowercomponents.registry;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlockEntity;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxItem;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;

public class MTArmInteractionPointTypes {
	private static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES =
		DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, FirepowerComponents.MOD_ID);

	public static final DeferredHolder<ArmInteractionPointType, CompactCannonMountType> COMPACT_CANNON_MOUNT =
		ARM_INTERACTION_POINT_TYPES.register("compact_cannon_mount", CompactCannonMountType::new);
	public static final DeferredHolder<ArmInteractionPointType, AutocannonAmmoFeedType> AUTOCANNON_AMMO_FEED =
		ARM_INTERACTION_POINT_TYPES.register("autocannon_ammo_feed", AutocannonAmmoFeedType::new);
	public static final DeferredHolder<ArmInteractionPointType, CannonMagazineLoaderType> CANNON_MAGAZINE_LOADER =
		ARM_INTERACTION_POINT_TYPES.register("cannon_magazine_loader", CannonMagazineLoaderType::new);

	public static void register(IEventBus bus) {
		ARM_INTERACTION_POINT_TYPES.register(bus);
	}

	public static class CompactCannonMountType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return (MTBlocks.COMPACT_CANNON_MOUNT.get() == state.getBlock()
				|| MTBlocks.COMPACT_AUTOCANNON_MOUNT.get() == state.getBlock())
				&& level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity;
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CompactCannonMountPoint(this, level, pos, state);
		}
	}

	public static class AutocannonAmmoFeedType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return MTBlocks.AUTOCANNON_AMMO_FEED.get() == state.getBlock()
				&& level.getBlockEntity(pos) instanceof AutocannonAmmoFeedBlockEntity;
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class CannonMagazineLoaderType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return MTBlocks.CANNON_MAGAZINE_LOADER.get() == state.getBlock()
				&& level.getBlockEntity(pos) instanceof CannonMagazineLoaderBlockEntity;
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CannonMagazineLoaderPoint(this, level, pos, state);
		}
	}

	public static class CannonMagazineLoaderPoint extends DepositOnlyArmInteractionPoint {
		public CannonMagazineLoaderPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atCenterOf(this.pos).add(0, 0.1, 0);
		}

		@Override
		public ItemStack insert(ArmBlockEntity be, ItemStack stack, boolean simulate) {
			BlockEntity blockEntity = this.getLevel().getBlockEntity(this.pos);
			return blockEntity instanceof CannonMagazineLoaderBlockEntity loader ? loader.insertAutomation(stack, simulate) : stack;
		}

		@Override
		public ItemStack extract(ArmBlockEntity be, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extract(ArmBlockEntity be, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}
	}

	public static class CompactCannonMountPoint extends CannonMountPoint {
		public CompactCannonMountPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			BlockEntity be = this.getLevel().getBlockEntity(this.pos);
			if (be instanceof CompactCannonMountBlockEntity mount)
				return mount.getInteractionLocation();
			return super.getInteractionPositionVector();
		}

		@Override
		public ItemStack insert(ArmBlockEntity be, ItemStack stack, boolean simulate) {
			BlockEntity targetBE = this.getLevel().getBlockEntity(this.pos);
			if (!(targetBE instanceof CompactCannonMountBlockEntity mount))
				return stack;
			PitchOrientedContraptionEntity poce = mount.getContraption();
			if (poce == null || !(poce.getContraption() instanceof AbstractMountedCannonContraption cannon))
				return stack;
			if (cannon instanceof MountedAutocannonContraption autocannon) {
				ItemStack insertedContainer = tryInsertAutocannonAmmoContainer(stack, simulate, autocannon);
				if (insertedContainer.getCount() != stack.getCount() || !ItemStack.matches(insertedContainer, stack))
					return insertedContainer;
				ItemStack insertedAmmo = tryInsertLooseAutocannonAmmo(stack, simulate, autocannon);
				if (insertedAmmo.getCount() != stack.getCount())
					return insertedAmmo;
			}
			if (cannon instanceof ItemCannon itemCannon)
				return itemCannon.insertItemIntoCannon(stack, simulate);
			return this.getInsertedResultAndDoSomething(stack, simulate, cannon, poce);
		}

		private static ItemStack tryInsertLooseAutocannonAmmo(ItemStack stack, boolean simulate, MountedAutocannonContraption cannon) {
			if (!(stack.getItem() instanceof AutocannonAmmoItem))
				return stack;
			AbstractAutocannonBreechBlockEntity breech = findAutocannonBreech(cannon);
			if (breech == null || breech.isInputFull())
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

		private static ItemStack tryInsertAutocannonAmmoContainer(ItemStack stack, boolean simulate, MountedAutocannonContraption cannon) {
			if (!(stack.getItem() instanceof AutocannonAmmoContainerItem))
				return stack;
			AbstractAutocannonBreechBlockEntity breech = findAutocannonBreech(cannon);
			if (breech == null)
				return stack;
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

		@Nullable
		private static AbstractAutocannonBreechBlockEntity findAutocannonBreech(MountedAutocannonContraption cannon) {
			BlockEntity startBE = cannon.presentBlockEntities.get(cannon.getStartPos());
			if (startBE instanceof AbstractAutocannonBreechBlockEntity breech)
				return breech;
			for (BlockEntity be : cannon.presentBlockEntities.values()) {
				if (be instanceof AbstractAutocannonBreechBlockEntity breech)
					return breech;
			}
			return null;
		}
	}
}
