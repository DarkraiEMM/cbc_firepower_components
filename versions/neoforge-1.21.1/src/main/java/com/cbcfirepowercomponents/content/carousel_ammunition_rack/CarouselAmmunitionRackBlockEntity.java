package com.cbcfirepowercomponents.content.carousel_ammunition_rack;

import static com.cbcfirepowercomponents.content.CannonAmmunitionHelper.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.AmmunitionSelectionSource;
import com.cbcfirepowercomponents.content.MountedCannonAmmunitionTarget;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;

public class CarouselAmmunitionRackBlockEntity extends KineticBlockEntity implements AmmunitionSelectionSource {
	public static final int CAPACITY = 24;
	private final ItemStack[] projectiles = new ItemStack[CAPACITY];
	private final ItemStack[] propellants = new ItemStack[CAPACITY];
	private final IItemHandler itemHandler = new CarouselItemHandler();
	private ItemStack selectedProjectile = ItemStack.EMPTY;
	private ItemStack selectedPropellant = ItemStack.EMPTY;
	private int currentIndex;
	private int targetIndex = -1;
	private int insertionCursor;
	private float indexProgress;
	private int pendingPropellantIndex = -1;
	@Nullable private BlockPos pendingTarget;

	public CarouselAmmunitionRackBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.CAROUSEL_AMMUNITION_RACK.get(), pos, state);
		for (int i = 0; i < CAPACITY; ++i) {
			this.projectiles[i] = ItemStack.EMPTY;
			this.propellants[i] = ItemStack.EMPTY;
		}
	}

	public IItemHandler getItemHandler(@Nullable Direction side) {
		return this.itemHandler;
	}

	public ItemStack insert(ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return stack;
		int slot = isReadyAmmunition(stack) ? this.firstEmptySlot()
			: isPropellant(stack) ? this.firstWaitingPropellantSlot() : -1;
		if (slot < 0)
			return stack;
		ItemStack remainder = stack.copy();
		remainder.shrink(1);
		if (!simulate) {
			if (isReadyAmmunition(stack)) {
				this.projectiles[slot] = stack.copyWithCount(1);
				this.insertionCursor = Math.floorMod(slot + 1, CAPACITY);
			} else
				this.propellants[slot] = stack.copyWithCount(1);
			this.ensureSelection();
			this.recalculateTarget();
			this.setChangedAndSync();
		}
		return remainder;
	}

	@Override
	public boolean selectType(ItemStack projectile, ItemStack propellant) {
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i) || !sameRound(this.projectiles[i], this.propellants[i], projectile, propellant))
				continue;
			this.selectedProjectile = this.projectiles[i].copyWithCount(1);
			this.selectedPropellant = this.propellants[i].copyWithCount(1);
			this.recalculateTarget();
			this.setChangedAndSync();
			return true;
		}
		return false;
	}

	@Override
	public void selectNextType() {
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> types = this.getAvailableRoundTypes();
		if (types.isEmpty())
			return;
		int selected = -1;
		for (int i = 0; i < types.size(); ++i)
			if (types.get(i).selected()) {
				selected = i;
				break;
			}
		var next = types.get((selected + 1) % types.size());
		this.selectType(next.projectile(), next.propellant());
	}

	@Override
	public List<ReadyAmmunitionCompartmentBlockEntity.RoundType> getAvailableRoundTypes() {
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> types = new ArrayList<>();
		outer: for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i))
				continue;
			for (ReadyAmmunitionCompartmentBlockEntity.RoundType type : types) {
				if (!sameRound(type.projectile(), type.propellant(), this.projectiles[i], this.propellants[i]))
					continue;
				int index = types.indexOf(type);
				types.set(index, new ReadyAmmunitionCompartmentBlockEntity.RoundType(type.projectile(),
					type.propellant(), type.count() + 1, type.selected()));
				continue outer;
			}
			types.add(new ReadyAmmunitionCompartmentBlockEntity.RoundType(
				this.projectiles[i].copyWithCount(1), this.propellants[i].copyWithCount(1), 1,
				sameRound(this.projectiles[i], this.propellants[i], this.selectedProjectile, this.selectedPropellant)));
		}
		return types;
	}

	public List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> getSlotsSnapshot() {
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots = new ArrayList<>(CAPACITY);
		for (int i = 0; i < CAPACITY; ++i)
			slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
				this.projectiles[i].copy(), this.propellants[i].copy()));
		return slots;
	}

	public int getCurrentIndex() {
		return this.currentIndex;
	}

	public int getTargetIndex() {
		return this.targetIndex;
	}

	public ItemStack getProjectileForRender(int slot) {
		return slot >= 0 && slot < CAPACITY ? this.projectiles[slot] : ItemStack.EMPTY;
	}

	public ItemStack getPropellantForRender(int slot) {
		return slot >= 0 && slot < CAPACITY ? this.propellants[slot] : ItemStack.EMPTY;
	}

	public float getVisualIndex(float partialTick) {
		if (this.targetIndex < 0 || this.currentIndex == this.targetIndex || Math.abs(this.getSpeed()) < 1.0f)
			return this.currentIndex;
		int clockwise = Math.floorMod(this.targetIndex - this.currentIndex, CAPACITY);
		int step = clockwise <= CAPACITY / 2 ? 1 : -1;
		float progress = Math.min(1.0f, this.indexProgress + partialTick * Math.abs(this.getSpeed()) / 128.0f);
		return this.currentIndex + step * progress;
	}

	@Override
	public AABB getRenderBoundingBox() {
		return new AABB(this.worldPosition.getX() - 1, this.worldPosition.getY(),
			this.worldPosition.getZ() - 1, this.worldPosition.getX() + 2,
			this.worldPosition.getY() + 2, this.worldPosition.getZ() + 2);
	}

	public boolean swapSlots(int first, int second) {
		if (first < 0 || first >= CAPACITY || second < 0 || second >= CAPACITY)
			return false;
		ItemStack projectile = this.projectiles[first];
		ItemStack propellant = this.propellants[first];
		this.projectiles[first] = this.projectiles[second];
		this.propellants[first] = this.propellants[second];
		this.projectiles[second] = projectile;
		this.propellants[second] = propellant;
		if (this.pendingPropellantIndex == first)
			this.pendingPropellantIndex = second;
		else if (this.pendingPropellantIndex == second)
			this.pendingPropellantIndex = first;
		this.recalculateTarget();
		this.setChangedAndSync();
		return true;
	}

	@Nullable
	public ReadyAmmunitionCompartmentBlockEntity.RoundPair extractAlignedRound() {
		if (this.pendingPropellantIndex >= 0 || this.targetIndex < 0
			|| this.currentIndex != this.targetIndex || !this.isComplete(this.currentIndex))
			return null;
		int slot = this.currentIndex;
		var result = new ReadyAmmunitionCompartmentBlockEntity.RoundPair(
			this.projectiles[slot].copyWithCount(1), this.propellants[slot].copyWithCount(1));
		this.projectiles[slot] = ItemStack.EMPTY;
		this.propellants[slot] = ItemStack.EMPTY;
		this.ensureSelection();
		this.recalculateTarget();
		this.setChangedAndSync();
		return result;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level == null || this.level.isClientSide)
			return;
		this.updateOutputFacingAndTarget();
		this.tickIndexing();
		if (this.currentIndex == this.targetIndex)
			this.feedAlignedRound();
	}

	private void tickIndexing() {
		boolean indexing = this.targetIndex >= 0 && this.currentIndex != this.targetIndex;
		BlockState state = this.getBlockState();
		if (state.getValue(CarouselAmmunitionRackBlock.INDEXING) != indexing)
			this.level.setBlock(this.worldPosition,
				state.setValue(CarouselAmmunitionRackBlock.INDEXING, indexing), 3);
		if (!indexing || Math.abs(this.getSpeed()) < 1.0f)
			return;
		this.indexProgress += Math.abs(this.getSpeed()) / 128.0f;
		if (this.indexProgress < 1.0f)
			return;
		this.indexProgress -= 1.0f;
		this.currentIndex = Math.floorMod(this.currentIndex + 1, CAPACITY);
		this.recalculateTarget();
		this.setChangedAndSync();
	}

	private void updateOutputFacingAndTarget() {
		BlockPos target = this.findOutputTarget();
		if (target == null)
			return;
		Direction facing = this.getBlockState().getValue(CarouselAmmunitionRackBlock.FACING);
		if (target.getY() == this.worldPosition.getY()) {
			int dx = target.getX() - this.worldPosition.getX();
			int dz = target.getZ() - this.worldPosition.getZ();
			facing = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST)
				: (dz > 0 ? Direction.SOUTH : Direction.NORTH);
		} else if (this.level.getBlockState(target).hasProperty(CompactCannonMountBlock.HORIZONTAL_FACING)) {
			facing = this.level.getBlockState(target).getValue(CompactCannonMountBlock.HORIZONTAL_FACING);
		}
		if (facing != this.getBlockState().getValue(CarouselAmmunitionRackBlock.FACING))
			this.level.setBlock(this.worldPosition,
				this.getBlockState().setValue(CarouselAmmunitionRackBlock.FACING, facing), 3);
	}

	@Nullable
	private BlockPos findOutputTarget() {
		if (this.level == null)
			return null;
		if (MountedCannonAmmunitionTarget.isMountedTarget(this.level.getBlockEntity(this.worldPosition.above())))
			return this.worldPosition.above();
		Direction preferred = this.getBlockState().getValue(CarouselAmmunitionRackBlock.FACING);
		if (MountedCannonAmmunitionTarget.isMountedTarget(
			this.level.getBlockEntity(this.worldPosition.relative(preferred, 2))))
			return this.worldPosition.relative(preferred, 2);
		for (Direction direction : Direction.Plane.HORIZONTAL)
			if (MountedCannonAmmunitionTarget.isMountedTarget(
				this.level.getBlockEntity(this.worldPosition.relative(direction, 2))))
				return this.worldPosition.relative(direction, 2);
		return null;
	}

	private void feedAlignedRound() {
		BlockPos targetPos = this.pendingTarget != null ? this.pendingTarget : this.findOutputTarget();
		BlockEntity target = targetPos == null ? null : this.level.getBlockEntity(targetPos);
		if (targetPos == null || !MountedCannonAmmunitionTarget.isMountedTarget(target))
			return;
		if (this.pendingPropellantIndex >= 0) {
			int slot = this.pendingPropellantIndex;
			ItemStack original = this.propellants[slot];
			ItemStack returned = MountedCannonAmmunitionTarget.insert(target, original, false);
			if (!wasAccepted(original, returned))
				return;
			this.handleAcceptedReturn(original, returned, targetPos);
			this.propellants[slot] = ItemStack.EMPTY;
			this.pendingPropellantIndex = -1;
			this.pendingTarget = null;
			this.ensureSelection();
			this.recalculateTarget();
			this.setChangedAndSync();
			return;
		}
		if (this.currentIndex < 0 || !this.isComplete(this.currentIndex))
			return;
		ItemStack original = this.projectiles[this.currentIndex];
		ItemStack returned = MountedCannonAmmunitionTarget.insert(target, original, false);
		if (!wasAccepted(original, returned))
			return;
		this.handleAcceptedReturn(original, returned, targetPos);
		this.projectiles[this.currentIndex] = ItemStack.EMPTY;
		if (isSelfContainedRound(original)) {
			this.ejectUnexpectedPropellant(this.currentIndex);
			this.ensureSelection();
			this.recalculateTarget();
		} else {
			this.pendingPropellantIndex = this.currentIndex;
			this.pendingTarget = targetPos;
		}
		this.setChangedAndSync();
	}

	private void handleAcceptedReturn(ItemStack inserted, ItemStack returned, BlockPos targetPos) {
		if (returned.isEmpty() || ItemStack.isSameItemSameComponents(inserted, returned))
			return;
		Containers.dropItemStack(this.level, targetPos.getX() + 0.5, targetPos.getY() + 0.5,
			targetPos.getZ() + 0.5, returned.copy());
	}

	public ItemStack getAutomationOutput(int slot) {
		if (slot == 1) {
			if (this.pendingPropellantIndex < 0 || this.pendingTarget != null
				|| this.pendingPropellantIndex >= CAPACITY)
				return ItemStack.EMPTY;
			ItemStack propellant = this.propellants[this.pendingPropellantIndex];
			return propellant.isEmpty() ? ItemStack.EMPTY : propellant.copyWithCount(1);
		}
		if (slot != 0 || this.pendingPropellantIndex >= 0 || this.targetIndex < 0
			|| this.currentIndex != this.targetIndex || !this.isComplete(this.currentIndex))
			return ItemStack.EMPTY;
		return this.projectiles[this.currentIndex].copyWithCount(1);
	}

	public ItemStack extractAutomationOutput(int slot, int amount, boolean simulate) {
		if (amount <= 0)
			return ItemStack.EMPTY;
		ItemStack available = this.getAutomationOutput(slot);
		if (available.isEmpty())
			return ItemStack.EMPTY;
		if (simulate)
			return available;
		if (slot == 1) {
			this.propellants[this.pendingPropellantIndex] = ItemStack.EMPTY;
			this.pendingPropellantIndex = -1;
			this.pendingTarget = null;
			this.ensureSelection();
			this.recalculateTarget();
		} else {
			int extractedIndex = this.currentIndex;
			this.projectiles[extractedIndex] = ItemStack.EMPTY;
			if (isSelfContainedRound(available)) {
				this.ejectUnexpectedPropellant(extractedIndex);
				this.ensureSelection();
				this.recalculateTarget();
			} else {
				this.pendingPropellantIndex = extractedIndex;
				this.pendingTarget = null;
			}
		}
		this.setChangedAndSync();
		return available;
	}

	private void ensureSelection() {
		if (!this.selectedProjectile.isEmpty())
			for (int i = 0; i < CAPACITY; ++i)
				if (this.isComplete(i) && sameRound(this.projectiles[i], this.propellants[i],
					this.selectedProjectile, this.selectedPropellant))
					return;
		for (int i = 0; i < CAPACITY; ++i)
			if (this.isComplete(i)) {
				this.selectedProjectile = this.projectiles[i].copyWithCount(1);
				this.selectedPropellant = this.propellants[i].copyWithCount(1);
				return;
			}
		this.selectedProjectile = ItemStack.EMPTY;
		this.selectedPropellant = ItemStack.EMPTY;
	}

	private void recalculateTarget() {
		this.targetIndex = -1;
		boolean hasSelectedRound = false;
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i) || !sameRound(this.projectiles[i], this.propellants[i],
				this.selectedProjectile, this.selectedPropellant))
				continue;
			hasSelectedRound = true;
			break;
		}
		if (!hasSelectedRound)
			return;
		this.targetIndex = this.isComplete(this.currentIndex)
			&& sameRound(this.projectiles[this.currentIndex], this.propellants[this.currentIndex],
				this.selectedProjectile, this.selectedPropellant)
			? this.currentIndex : Math.floorMod(this.currentIndex + 1, CAPACITY);
	}

	private boolean isComplete(int slot) {
		return isSelfContainedRound(this.projectiles[slot])
			? true
			: isLoadReadyProjectile(this.projectiles[slot]) && isPropellant(this.propellants[slot]);
	}

	private void ejectUnexpectedPropellant(int slot) {
		ItemStack extra = this.propellants[slot];
		this.propellants[slot] = ItemStack.EMPTY;
		if (this.level != null && !extra.isEmpty())
			Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5,
				this.worldPosition.getY() + 0.75, this.worldPosition.getZ() + 0.5, extra);
	}

	private int firstEmptySlot() {
		for (int offset = 0; offset < CAPACITY; ++offset) {
			int slot = Math.floorMod(this.insertionCursor + offset, CAPACITY);
			if (this.projectiles[slot].isEmpty() && this.propellants[slot].isEmpty())
				return slot;
		}
		return -1;
	}

	private int firstWaitingPropellantSlot() {
		for (int i = 0; i < CAPACITY; ++i)
			if (requiresSeparatePropellant(this.projectiles[i]) && this.propellants[i].isEmpty())
				return i;
		return -1;
	}

	public void dropContents() {
		if (this.level == null)
			return;
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.projectiles[i].isEmpty())
				Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(),
					this.worldPosition.getZ(), this.projectiles[i]);
			if (!this.propellants[i].isEmpty())
				Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(),
					this.worldPosition.getZ(), this.propellants[i]);
			this.projectiles[i] = ItemStack.EMPTY;
			this.propellants[i] = ItemStack.EMPTY;
		}
	}

	private void setChangedAndSync() {
		this.setChanged();
		if (this.level != null)
			this.sendData();
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		ListTag rounds = new ListTag();
		for (int i = 0; i < CAPACITY; ++i) {
			if (this.projectiles[i].isEmpty() && this.propellants[i].isEmpty())
				continue;
			CompoundTag round = new CompoundTag();
			round.putByte("Slot", (byte) i);
			if (!this.projectiles[i].isEmpty()) round.put("Projectile", this.projectiles[i].saveOptional(registries));
			if (!this.propellants[i].isEmpty()) round.put("Propellant", this.propellants[i].saveOptional(registries));
			rounds.add(round);
		}
		tag.put("Rounds", rounds);
		tag.putInt("CurrentIndex", this.currentIndex);
		tag.putInt("TargetIndex", this.targetIndex);
		tag.putInt("InsertionCursor", this.insertionCursor);
		tag.putFloat("IndexProgress", this.indexProgress);
		if (!this.selectedProjectile.isEmpty())
			tag.put("SelectedProjectile", this.selectedProjectile.saveOptional(registries));
		if (!this.selectedPropellant.isEmpty())
			tag.put("SelectedPropellant", this.selectedPropellant.saveOptional(registries));
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		for (int i = 0; i < CAPACITY; ++i) {
			this.projectiles[i] = ItemStack.EMPTY;
			this.propellants[i] = ItemStack.EMPTY;
		}
		ListTag rounds = tag.getList("Rounds", 10);
		for (int i = 0; i < rounds.size(); ++i) {
			CompoundTag round = rounds.getCompound(i);
			int slot = round.getByte("Slot") & 255;
			if (slot >= CAPACITY) continue;
			if (round.contains("Projectile"))
				this.projectiles[slot] = ItemStack.parseOptional(registries, round.getCompound("Projectile"));
			if (round.contains("Propellant"))
				this.propellants[slot] = ItemStack.parseOptional(registries, round.getCompound("Propellant"));
		}
		this.currentIndex = Math.floorMod(tag.getInt("CurrentIndex"), CAPACITY);
		this.targetIndex = tag.getInt("TargetIndex");
		if (this.targetIndex < -1 || this.targetIndex >= CAPACITY)
			this.targetIndex = -1;
		this.insertionCursor = Math.floorMod(tag.getInt("InsertionCursor"), CAPACITY);
		this.indexProgress = tag.getFloat("IndexProgress");
		this.selectedProjectile = tag.contains("SelectedProjectile")
			? ItemStack.parseOptional(registries, tag.getCompound("SelectedProjectile")) : ItemStack.EMPTY;
		this.selectedPropellant = tag.contains("SelectedPropellant")
			? ItemStack.parseOptional(registries, tag.getCompound("SelectedPropellant")) : ItemStack.EMPTY;
		this.pendingPropellantIndex = -1;
		this.pendingTarget = null;
	}

	private class CarouselItemHandler implements IItemHandler {
		@Override public int getSlots() { return 2; }
		@Override public ItemStack getStackInSlot(int slot) {
			return CarouselAmmunitionRackBlockEntity.this.getAutomationOutput(slot);
		}
		@Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot == 0 && isReadyAmmunition(stack) || slot == 1 && isPropellant(stack))
				return CarouselAmmunitionRackBlockEntity.this.insert(stack, simulate);
			return stack;
		}
		@Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return CarouselAmmunitionRackBlockEntity.this.extractAutomationOutput(slot, amount, simulate);
		}
		@Override public int getSlotLimit(int slot) { return 1; }
		@Override public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 && isReadyAmmunition(stack) || slot == 1 && isPropellant(stack);
		}
	}
}
