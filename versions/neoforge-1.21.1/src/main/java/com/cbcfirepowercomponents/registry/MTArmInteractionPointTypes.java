package com.cbcfirepowercomponents.registry;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.autocannon_ammo_feed.AutocannonAmmoFeedBlockEntity;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackStructuralBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlockEntity;
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
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;

public class MTArmInteractionPointTypes {
	private static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES =
		DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, FirepowerComponents.MOD_ID);

	public static final DeferredHolder<ArmInteractionPointType, CompactCannonMountType> COMPACT_CANNON_MOUNT =
		ARM_INTERACTION_POINT_TYPES.register("compact_cannon_mount", CompactCannonMountType::new);
	public static final DeferredHolder<ArmInteractionPointType, AutocannonAmmoFeedType> AUTOCANNON_AMMO_FEED =
		ARM_INTERACTION_POINT_TYPES.register("autocannon_ammo_feed", AutocannonAmmoFeedType::new);
	public static final DeferredHolder<ArmInteractionPointType, CannonMagazineLoaderType> CANNON_MAGAZINE_LOADER =
		ARM_INTERACTION_POINT_TYPES.register("cannon_magazine_loader", CannonMagazineLoaderType::new);
	public static final DeferredHolder<ArmInteractionPointType, ReadyAmmunitionCompartmentType> READY_AMMUNITION_COMPARTMENT =
		ARM_INTERACTION_POINT_TYPES.register("ready_ammunition_compartment", ReadyAmmunitionCompartmentType::new);
	public static final DeferredHolder<ArmInteractionPointType, CarouselAmmunitionRackType> CAROUSEL_AMMUNITION_RACK =
		ARM_INTERACTION_POINT_TYPES.register("carousel_ammunition_rack", CarouselAmmunitionRackType::new);
	public static final DeferredHolder<ArmInteractionPointType, SpentCasingCollectorType> SPENT_CASING_COLLECTOR =
		ARM_INTERACTION_POINT_TYPES.register("spent_casing_collector", SpentCasingCollectorType::new);

	public static void register(IEventBus bus) {
		ARM_INTERACTION_POINT_TYPES.register(bus);
	}

	public static class CompactCannonMountType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return (MTBlocks.COMPACT_CANNON_MOUNT.get() == state.getBlock()
				|| MTBlocks.COMPACT_AUTOCANNON_MOUNT.get() == state.getBlock()
				|| MTBlocks.VERTICAL_COMPACT_CANNON_MOUNT.get() == state.getBlock())
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

	public static class ReadyAmmunitionCompartmentType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return MTBlocks.READY_AMMUNITION_COMPARTMENT.get() == state.getBlock()
				&& level.getBlockEntity(pos) instanceof ReadyAmmunitionCompartmentBlockEntity;
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class CarouselAmmunitionRackType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(MTBlocks.CAROUSEL_AMMUNITION_RACK.get())
				|| state.is(MTBlocks.CAROUSEL_AMMUNITION_RACK_STRUCTURE.get());
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CarouselAmmunitionRackPoint(this, level, pos, state);
		}
	}

	public static class SpentCasingCollectorType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return MTBlocks.SPENT_CASING_COLLECTOR.get() == state.getBlock()
				&& level.getBlockEntity(pos) instanceof SpentCasingCollectorBlockEntity;
		}

		@Nullable
		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new SpentCasingCollectorPoint(this, level, pos, state);
		}
	}

	public static class SpentCasingCollectorPoint extends ArmInteractionPoint {
		public SpentCasingCollectorPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atCenterOf(this.pos).add(0, 0.35, 0);
		}
	}

	public static class CarouselAmmunitionRackPoint extends ArmInteractionPoint {
		public CarouselAmmunitionRackPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		private BlockPos corePos() {
			BlockState state = this.getLevel().getBlockState(this.pos);
			return state.getBlock() instanceof CarouselAmmunitionRackStructuralBlock
				? CarouselAmmunitionRackStructuralBlock.corePos(this.pos, state) : this.pos;
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atCenterOf(this.pos).add(0, 0.15, 0);
		}

		@Override
		public ItemStack insert(ArmBlockEntity be, ItemStack stack, boolean simulate) {
			BlockEntity blockEntity = this.getLevel().getBlockEntity(this.corePos());
			return blockEntity instanceof CarouselAmmunitionRackBlockEntity rack ? rack.insert(stack, simulate) : stack;
		}

		@Override public ItemStack extract(ArmBlockEntity be, int slot, int amount, boolean simulate) {
			BlockEntity blockEntity = this.getLevel().getBlockEntity(this.corePos());
			return blockEntity instanceof CarouselAmmunitionRackBlockEntity rack
				? rack.extractAutomationOutput(slot, amount, simulate) : ItemStack.EMPTY;
		}
		@Override public ItemStack extract(ArmBlockEntity be, int amount, boolean simulate) {
			BlockEntity blockEntity = this.getLevel().getBlockEntity(this.corePos());
			if (!(blockEntity instanceof CarouselAmmunitionRackBlockEntity rack))
				return ItemStack.EMPTY;
			ItemStack projectile = rack.extractAutomationOutput(0, amount, simulate);
			return projectile.isEmpty() ? rack.extractAutomationOutput(1, amount, simulate) : projectile;
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
			PitchOrientedContraptionEntity entity = mount.getContraption();
			if (entity == null || !(entity.getContraption() instanceof AbstractMountedCannonContraption cannon))
				return stack;
			return this.getInsertedResultAndDoSomething(stack, simulate, cannon, entity);
		}

	}
}
