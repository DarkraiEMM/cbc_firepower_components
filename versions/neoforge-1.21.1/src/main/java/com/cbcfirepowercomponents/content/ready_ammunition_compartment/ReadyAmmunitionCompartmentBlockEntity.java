package com.cbcfirepowercomponents.content.ready_ammunition_compartment;

import static com.cbcfirepowercomponents.content.CannonAmmunitionHelper.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.MountedCannonAmmunitionTarget;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.AmmunitionSelectionSource;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class ReadyAmmunitionCompartmentBlockEntity extends BlockEntity implements AmmunitionSelectionSource {
	public static final int CAPACITY = 40;
	private final ItemStack[] projectiles = new ItemStack[CAPACITY];
	private final ItemStack[] propellants = new ItemStack[CAPACITY];
	private final IItemHandler itemHandler = new InputHandler();
	private ItemStack selectedProjectile = ItemStack.EMPTY;
	private ItemStack selectedPropellant = ItemStack.EMPTY;
	private int pendingGroup = -1;
	private int insertionCursor;
	@Nullable private Direction pendingDirection;
	private int transferCooldown;

	public ReadyAmmunitionCompartmentBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.READY_AMMUNITION_COMPARTMENT.get(), pos, state);
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
		int slot;
		if (isReadyAmmunition(stack)) {
			slot = this.firstEmptyProjectileSlot();
		} else if (isPropellant(stack)) {
			slot = this.firstWaitingPropellantSlot();
		} else {
			return stack;
		}
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
			this.setChangedAndSync();
		}
		return remainder;
	}

	public void selectNextType() {
		List<Integer> types = this.distinctTypeGroups();
		if (types.isEmpty()) {
			this.selectedProjectile = ItemStack.EMPTY;
			this.selectedPropellant = ItemStack.EMPTY;
			this.setChangedAndSync();
			return;
		}
		int current = -1;
		for (int i = 0; i < types.size(); ++i) {
			int group = types.get(i);
			if (sameRound(this.projectiles[group], this.propellants[group],
				this.selectedProjectile, this.selectedPropellant)) {
				current = i;
				break;
			}
		}
		int nextGroup = types.get((current + 1) % types.size());
		this.selectedProjectile = this.projectiles[nextGroup].copyWithCount(1);
		this.selectedPropellant = this.propellants[nextGroup].copyWithCount(1);
		this.setChangedAndSync();
	}

	public boolean selectType(ItemStack projectile, ItemStack propellant) {
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i) || !sameRound(this.projectiles[i], this.propellants[i], projectile, propellant))
				continue;
			this.selectedProjectile = this.projectiles[i].copyWithCount(1);
			this.selectedPropellant = this.propellants[i].copyWithCount(1);
			this.setChangedAndSync();
			return true;
		}
		return false;
	}

	public List<RoundPair> getRoundSlotsSnapshot() {
		List<RoundPair> slots = new ArrayList<>(CAPACITY);
		for (int i = 0; i < CAPACITY; ++i)
			slots.add(new RoundPair(this.projectiles[i].copy(), this.propellants[i].copy()));
		return slots;
	}

	public List<RoundType> getAvailableRoundTypes() {
		List<RoundType> types = new ArrayList<>();
		for (int group : this.distinctTypeGroups()) {
			int count = 0;
			for (int i = 0; i < CAPACITY; ++i)
				if (this.isComplete(i) && sameRound(this.projectiles[i], this.propellants[i],
					this.projectiles[group], this.propellants[group]))
					++count;
			boolean selected = sameRound(this.projectiles[group], this.propellants[group],
				this.selectedProjectile, this.selectedPropellant);
			types.add(new RoundType(this.projectiles[group].copyWithCount(1),
				this.propellants[group].copyWithCount(1), count, selected));
		}
		return types;
	}

	public boolean swapRoundSlots(int first, int second) {
		if (first < 0 || first >= CAPACITY || second < 0 || second >= CAPACITY)
			return false;
		if (first == second)
			return true;
		ItemStack projectile = this.projectiles[first];
		ItemStack propellant = this.propellants[first];
		this.projectiles[first] = this.projectiles[second];
		this.propellants[first] = this.propellants[second];
		this.projectiles[second] = projectile;
		this.propellants[second] = propellant;
		// Slot zero is presented as the green queue head in the rack screen.
		// Moving a complete round into that working slot must also change the
		// selected type; otherwise the screen swaps visibly while the rack keeps
		// feeding the previously selected ammunition from another slot.
		if ((first == 0 || second == 0) && this.isComplete(0)) {
			this.selectedProjectile = this.projectiles[0].copyWithCount(1);
			this.selectedPropellant = this.propellants[0].copyWithCount(1);
		}
		if (this.pendingGroup == first)
			this.pendingGroup = second;
		else if (this.pendingGroup == second)
			this.pendingGroup = first;
		this.setChangedAndSync();
		return true;
	}

	@Nullable
	public RoundPair extractSelectedRound() {
		int group = this.findSelectedCompleteGroup();
		if (group < 0)
			return null;
		RoundPair result = new RoundPair(this.projectiles[group], this.propellants[group]);
		this.projectiles[group] = ItemStack.EMPTY;
		this.propellants[group] = ItemStack.EMPTY;
		this.compact();
		this.ensureSelection();
		this.setChangedAndSync();
		return result;
	}

	public Component getStatusMessage() {
		int total = this.completeRoundCount();
		int selected = this.selectedRoundCount();
		if (this.selectedProjectile.isEmpty())
			return Component.translatable("block.cbc_firepower_components.ready_ammunition_compartment.status_empty", total, CAPACITY);
		return Component.translatable("block.cbc_firepower_components.ready_ammunition_compartment.status",
			this.selectedProjectile.getHoverName(), selected, total, CAPACITY);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ReadyAmmunitionCompartmentBlockEntity compartment) {
		compartment.updateOccupiedState();
		if (compartment.transferCooldown-- > 0)
			return;
		compartment.transferCooldown = 2;
		compartment.transferSelectedRound();
	}

	private void transferSelectedRound() {
		if (this.level == null)
			return;
		if (this.pendingGroup >= 0 && this.pendingDirection != null) {
			if (this.pendingGroup >= CAPACITY || this.propellants[this.pendingGroup].isEmpty()) {
				this.clearPending();
				return;
			}
			BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(this.pendingDirection));
			if (!this.isAmmunitionTarget(target)) {
				this.clearPending();
				return;
			}
			ItemStack original = this.propellants[this.pendingGroup];
			ItemStack returned = this.insertIntoTarget(target, original, false);
			if (!wasAccepted(original, returned))
				return;
			this.propellants[this.pendingGroup] = this.resolveAcceptedReturn(original, returned, this.pendingDirection);
			this.projectiles[this.pendingGroup] = ItemStack.EMPTY;
			this.clearPending();
			this.compact();
			this.ensureSelection();
			this.setChangedAndSync();
			return;
		}
		// A null direction with a pending group means external automation has
		// already taken the projectile and must take its matching propellant next.
		if (this.pendingGroup >= 0)
			return;

		int group = this.findSelectedCompleteGroup();
		if (group < 0)
			return;

		// A ready rack feeds an assembled mount directly. The external three-round
		// loader remains supported as an optional buffer, not as a required link.
		for (Direction direction : Direction.values()) {
			BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(direction));
			if (!MountedCannonAmmunitionTarget.isMountedTarget(target))
				continue;
			if (this.beginTransfer(group, direction, target))
				return;
		}
		for (Direction direction : Direction.values()) {
			BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(direction));
			if (!(target instanceof CannonMagazineLoaderBlockEntity loader))
				continue;
			if (this.beginTransfer(group, direction, loader))
				return;
		}
	}

	private boolean beginTransfer(int group, Direction direction, BlockEntity target) {
		ItemStack original = this.projectiles[group];
		ItemStack returned = this.insertIntoTarget(target, original, false);
		if (!wasAccepted(original, returned))
			return false;
		this.projectiles[group] = this.resolveAcceptedReturn(original, returned, direction);
		if (isSelfContainedRound(original)) {
			this.ejectUnexpectedPropellant(group);
			this.compact();
			this.ensureSelection();
			this.setChangedAndSync();
			return true;
		}
		this.pendingGroup = group;
		this.pendingDirection = direction;
		this.setChangedAndSync();
		return true;
	}

	/**
	 * CBC loading methods may return an item displaced from the breech (most
	 * notably an empty big-cartridge casing) instead of a remainder of the
	 * inserted stack. Keep true remainders, but eject exchanged items into the
	 * world so a nearby casing collector can pick them up.
	 */
	private ItemStack resolveAcceptedReturn(ItemStack inserted, ItemStack returned, Direction targetDirection) {
		if (returned.isEmpty())
			return ItemStack.EMPTY;
		if (ItemStack.isSameItemSameComponents(inserted, returned))
			return returned;
		if (this.level != null) {
			BlockPos outputPos = this.worldPosition.relative(targetDirection);
			Containers.dropItemStack(this.level, outputPos.getX() + 0.5, outputPos.getY() + 0.5,
				outputPos.getZ() + 0.5, returned.copy());
		}
		return ItemStack.EMPTY;
	}

	private boolean isAmmunitionTarget(@Nullable BlockEntity target) {
		return MountedCannonAmmunitionTarget.isMountedTarget(target)
			|| target instanceof CannonMagazineLoaderBlockEntity;
	}

	private ItemStack insertIntoTarget(BlockEntity target, ItemStack stack, boolean simulate) {
		if (MountedCannonAmmunitionTarget.isMountedTarget(target))
			return MountedCannonAmmunitionTarget.insert(target, stack, simulate);
		if (target instanceof CannonMagazineLoaderBlockEntity loader)
			return loader.insertAutomation(stack, simulate);
		return stack;
	}

	private void clearPending() {
		this.pendingGroup = -1;
		this.pendingDirection = null;
	}

	private ItemStack getAutomationOutput(int slot) {
		if (slot == 1) {
			if (this.pendingGroup < 0 || this.pendingDirection != null || this.pendingGroup >= CAPACITY)
				return ItemStack.EMPTY;
			return this.propellants[this.pendingGroup].isEmpty()
				? ItemStack.EMPTY : this.propellants[this.pendingGroup].copyWithCount(1);
		}
		if (slot != 0 || this.pendingGroup >= 0)
			return ItemStack.EMPTY;
		int group = this.findSelectedCompleteGroup();
		return group < 0 ? ItemStack.EMPTY : this.projectiles[group].copyWithCount(1);
	}

	private ItemStack extractAutomationOutput(int slot, int amount, boolean simulate) {
		if (amount <= 0)
			return ItemStack.EMPTY;
		if (slot == 1) {
			if (this.pendingGroup < 0)
				return ItemStack.EMPTY;
			if (this.pendingDirection != null || this.pendingGroup >= CAPACITY
				|| this.propellants[this.pendingGroup].isEmpty())
				return ItemStack.EMPTY;
			ItemStack result = this.propellants[this.pendingGroup].copyWithCount(1);
			if (!simulate) {
				this.propellants[this.pendingGroup] = ItemStack.EMPTY;
				this.clearPending();
				this.compact();
				this.ensureSelection();
				this.setChangedAndSync();
			}
			return result;
		}
		if (slot != 0 || this.pendingGroup >= 0)
			return ItemStack.EMPTY;
		int group = this.findSelectedCompleteGroup();
		if (group < 0)
			return ItemStack.EMPTY;
		ItemStack result = this.projectiles[group].copyWithCount(1);
		if (!simulate) {
			this.projectiles[group] = ItemStack.EMPTY;
			if (isSelfContainedRound(result)) {
				this.ejectUnexpectedPropellant(group);
				this.compact();
				this.ensureSelection();
			} else {
				this.pendingGroup = group;
				this.pendingDirection = null;
			}
			this.setChangedAndSync();
		}
		return result;
	}

	private int firstEmptyProjectileSlot() {
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

	private int findSelectedCompleteGroup() {
		this.ensureSelection();
		for (int i = 0; i < CAPACITY; ++i)
			if (this.isComplete(i) && sameRound(this.projectiles[i], this.propellants[i],
				this.selectedProjectile, this.selectedPropellant))
				return i;
		return -1;
	}

	private void ensureSelection() {
		if (!this.selectedProjectile.isEmpty() && this.selectedRoundCount() > 0)
			return;
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i))
				continue;
			this.selectedProjectile = this.projectiles[i].copyWithCount(1);
			this.selectedPropellant = this.propellants[i].copyWithCount(1);
			return;
		}
		this.selectedProjectile = ItemStack.EMPTY;
		this.selectedPropellant = ItemStack.EMPTY;
	}

	private List<Integer> distinctTypeGroups() {
		List<Integer> groups = new ArrayList<>();
		outer: for (int i = 0; i < CAPACITY; ++i) {
			if (!this.isComplete(i))
				continue;
			for (int group : groups)
				if (sameRound(this.projectiles[i], this.propellants[i],
					this.projectiles[group], this.propellants[group]))
					continue outer;
			groups.add(i);
		}
		return groups;
	}

	private boolean isComplete(int group) {
		if (isSelfContainedRound(this.projectiles[group]))
			return true;
		return isLoadReadyProjectile(this.projectiles[group]) && isPropellant(this.propellants[group]);
	}

	private void ejectUnexpectedPropellant(int group) {
		ItemStack extra = this.propellants[group];
		this.propellants[group] = ItemStack.EMPTY;
		if (this.level != null && !extra.isEmpty())
			Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5,
				this.worldPosition.getY() + 0.75, this.worldPosition.getZ() + 0.5, extra);
	}

	private int completeRoundCount() {
		int count = 0;
		for (int i = 0; i < CAPACITY; ++i)
			if (this.isComplete(i))
				++count;
		return count;
	}

	private int selectedRoundCount() {
		if (this.selectedProjectile.isEmpty())
			return 0;
		int count = 0;
		for (int i = 0; i < CAPACITY; ++i)
			if (this.isComplete(i) && sameRound(this.projectiles[i], this.propellants[i],
				this.selectedProjectile, this.selectedPropellant))
				++count;
		return count;
	}

	private void compact() {
		int target = 0;
		for (int source = 0; source < CAPACITY; ++source) {
			if (this.projectiles[source].isEmpty() && this.propellants[source].isEmpty())
				continue;
			if (source != target) {
				this.projectiles[target] = this.projectiles[source];
				this.propellants[target] = this.propellants[source];
				this.projectiles[source] = ItemStack.EMPTY;
				this.propellants[source] = ItemStack.EMPTY;
			}
			++target;
		}
		this.insertionCursor = target % CAPACITY;
	}

	public void dropContents(Level level) {
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.projectiles[i].isEmpty())
				Containers.dropItemStack(level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.projectiles[i]);
			if (!this.propellants[i].isEmpty())
				Containers.dropItemStack(level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.propellants[i]);
			this.projectiles[i] = ItemStack.EMPTY;
			this.propellants[i] = ItemStack.EMPTY;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
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
		tag.putInt("InsertionCursor", this.insertionCursor);
		if (!this.selectedProjectile.isEmpty()) tag.put("SelectedProjectile", this.selectedProjectile.saveOptional(registries));
		if (!this.selectedPropellant.isEmpty()) tag.put("SelectedPropellant", this.selectedPropellant.saveOptional(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		for (int i = 0; i < CAPACITY; ++i) {
			this.projectiles[i] = ItemStack.EMPTY;
			this.propellants[i] = ItemStack.EMPTY;
		}
		ListTag rounds = tag.getList("Rounds", 10);
		for (int i = 0; i < rounds.size(); ++i) {
			CompoundTag round = rounds.getCompound(i);
			int slot = round.getByte("Slot") & 255;
			if (slot >= CAPACITY) continue;
			if (round.contains("Projectile")) this.projectiles[slot] = ItemStack.parseOptional(registries, round.getCompound("Projectile"));
			if (round.contains("Propellant")) this.propellants[slot] = ItemStack.parseOptional(registries, round.getCompound("Propellant"));
		}
		this.insertionCursor = Math.floorMod(tag.getInt("InsertionCursor"), CAPACITY);
		this.selectedProjectile = tag.contains("SelectedProjectile")
			? ItemStack.parseOptional(registries, tag.getCompound("SelectedProjectile")) : ItemStack.EMPTY;
		this.selectedPropellant = tag.contains("SelectedPropellant")
			? ItemStack.parseOptional(registries, tag.getCompound("SelectedPropellant")) : ItemStack.EMPTY;
		this.clearPending();
		this.ensureSelection();
	}

	private void setChangedAndSync() {
		this.setChanged();
		this.updateOccupiedState();
		if (this.level != null)
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
	}

	private void updateOccupiedState() {
		if (this.level == null || this.level.isClientSide)
			return;
		boolean occupied = false;
		for (int i = 0; i < CAPACITY; ++i) {
			if (!this.projectiles[i].isEmpty() || !this.propellants[i].isEmpty()) {
				occupied = true;
				break;
			}
		}
		BlockState state = this.getBlockState();
		if (state.hasProperty(ReadyAmmunitionCompartmentBlock.OCCUPIED)
			&& state.getValue(ReadyAmmunitionCompartmentBlock.OCCUPIED) != occupied)
			this.level.setBlock(this.worldPosition,
				state.setValue(ReadyAmmunitionCompartmentBlock.OCCUPIED, occupied), 3);
	}

	@Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		this.saveAdditional(tag, registries);
		return tag;
	}
	@Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public record RoundPair(ItemStack projectile, ItemStack propellant) {}
	public record RoundType(ItemStack projectile, ItemStack propellant, int count, boolean selected) {}

	private class InputHandler implements IItemHandler {
		@Override public int getSlots() { return 2; }
		@Override public ItemStack getStackInSlot(int slot) {
			return ReadyAmmunitionCompartmentBlockEntity.this.getAutomationOutput(slot);
		}
		@Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot == 0 && isReadyAmmunition(stack))
				return ReadyAmmunitionCompartmentBlockEntity.this.insert(stack, simulate);
			if (slot == 1 && isPropellant(stack))
				return ReadyAmmunitionCompartmentBlockEntity.this.insert(stack, simulate);
			return stack;
		}
		@Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ReadyAmmunitionCompartmentBlockEntity.this.extractAutomationOutput(slot, amount, simulate);
		}
		@Override public int getSlotLimit(int slot) { return 1; }
		@Override public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 && isReadyAmmunition(stack) || slot == 1 && isPropellant(stack);
		}
	}
}
