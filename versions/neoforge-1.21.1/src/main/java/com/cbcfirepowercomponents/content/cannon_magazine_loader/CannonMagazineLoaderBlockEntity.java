package com.cbcfirepowercomponents.content.cannon_magazine_loader;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.ExtendsCannonMount;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBehavior;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonMunitionBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCannonPropellantBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlockItem;

public class CannonMagazineLoaderBlockEntity extends BlockEntity {
	public static final int GROUP_COUNT = 3;
	public static final int PROJECTILE_SLOTS = GROUP_COUNT;
	public static final int SLOT_COUNT = GROUP_COUNT * 2;
	private static final int NO_PENDING_GROUP = -1;

	private final ItemStack[] items = new ItemStack[SLOT_COUNT];
	private final IItemHandler automationHandler = new AutomationItemHandler();
	private final IItemHandler shellOutputAutomationHandler = new ShellOutputAutomationItemHandler();
	private boolean automaticLocked;
	private boolean automationFillStarted;
	private int pendingCartridgeGroup = NO_PENDING_GROUP;
	@Nullable private Direction pendingTargetDirection;

	public CannonMagazineLoaderBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.CANNON_MAGAZINE_LOADER.get(), pos, state);
		for (int i = 0; i < SLOT_COUNT; ++i)
			this.items[i] = ItemStack.EMPTY;
	}

	@Nullable
	public IItemHandler getItemHandler(Direction side) {
		return this.isShellOutputSide(side) ? this.shellOutputAutomationHandler : this.automationHandler;
	}

	private boolean isShellOutputSide(@Nullable Direction side) {
		if (side == null)
			return false;
		if (side == Direction.DOWN)
			return true;
		if (side.getAxis() == Direction.Axis.Y)
			return false;
		Direction front = this.getBlockState().getValue(CannonMagazineLoaderBlock.FACING);
		return side != front;
	}

	public ItemStack getStackInSlot(int slot) {
		return slot >= 0 && slot < SLOT_COUNT ? this.items[slot] : ItemStack.EMPTY;
	}

	public ItemStack insertManual(ItemStack stack) {
		if (stack.isEmpty())
			return stack;
		int start = isProjectile(stack) ? 0 : isPropellant(stack) ? PROJECTILE_SLOTS : -1;
		int end = isProjectile(stack) ? PROJECTILE_SLOTS : isPropellant(stack) ? SLOT_COUNT : -1;
		if (start < 0)
			return stack;
		for (int slot = start; slot < end; ++slot) {
			if (!this.items[slot].isEmpty())
				continue;
			return this.insertIntoSlot(slot, stack, false);
		}
		return stack;
	}

	public ItemStack insertAutomation(ItemStack stack, boolean simulate) {
		int slot = this.findAutomationSlot(stack);
		if (slot < 0)
			return stack;
		if (!simulate)
			this.automationFillStarted = true;
		return this.insertIntoSlot(slot, stack, simulate);
	}

	public ItemStack extractManual(int slot) {
		if (slot < 0 || slot >= SLOT_COUNT || this.items[slot].isEmpty())
			return ItemStack.EMPTY;
		ItemStack extracted = this.items[slot].copy();
		this.items[slot] = ItemStack.EMPTY;
		this.normalizePendingState();
		this.setChangedAndSync();
		return extracted;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CannonMagazineLoaderBlockEntity loader) {
		loader.tickLoading();
	}

	private void tickLoading() {
		if (this.level == null)
			return;
		if (this.tryEjectSpentCartridge())
			return;
		if (this.isEmpty()) {
			boolean changed = this.automaticLocked || this.automationFillStarted
				|| this.pendingCartridgeGroup != NO_PENDING_GROUP || this.pendingTargetDirection != null;
			this.automaticLocked = false;
			this.automationFillStarted = false;
			this.pendingCartridgeGroup = NO_PENDING_GROUP;
			this.pendingTargetDirection = null;
			if (changed)
				this.setChangedAndSync();
			return;
		}
		if (this.automationFillStarted && !this.automaticLocked && !this.isFullyLoaded()) {
			return;
		}
		if (!this.automaticLocked) {
			if (this.isFullyLoaded()) {
				this.automaticLocked = true;
				this.setChangedAndSync();
			}
		}
		if (this.pendingCartridgeGroup != NO_PENDING_GROUP) {
			this.tryLoadPendingCartridge();
			return;
		}
		int group = this.findCompleteGroup();
		if (group != NO_PENDING_GROUP)
			this.tryLoadProjectile(group);
	}

	private void tryLoadProjectile(int group) {
		ItemStack projectile = this.items[group];
		if (projectile.isEmpty())
			return;
		for (Direction direction : Direction.values()) {
			BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(direction));
			ItemStack remainder = this.insertIntoMountedCannon(target, projectile, false);
			if (wasAccepted(projectile, remainder)) {
				this.items[group] = remainder;
				this.pendingCartridgeGroup = group;
				this.pendingTargetDirection = direction;
				this.automaticLocked = true;
				this.setChangedAndSync();
				return;
			}
		}
	}

	private void tryLoadPendingCartridge() {
		if (this.pendingTargetDirection == null || this.pendingCartridgeGroup < 0 || this.pendingCartridgeGroup >= GROUP_COUNT) {
			this.pendingCartridgeGroup = NO_PENDING_GROUP;
			this.pendingTargetDirection = null;
			this.setChangedAndSync();
			return;
		}
		int slot = cartridgeSlot(this.pendingCartridgeGroup);
		ItemStack cartridge = this.items[slot];
		if (cartridge.isEmpty()) {
			this.pendingCartridgeGroup = NO_PENDING_GROUP;
			this.pendingTargetDirection = null;
			this.setChangedAndSync();
			return;
		}
		BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(this.pendingTargetDirection));
		ItemStack remainder = this.insertIntoMountedCannon(target, cartridge, false);
		if (!wasAccepted(cartridge, remainder))
			return;
		this.items[slot] = remainder;
		this.pendingCartridgeGroup = NO_PENDING_GROUP;
		this.pendingTargetDirection = null;
		this.normalizePendingState();
		this.setChangedAndSync();
	}

	private ItemStack insertIntoMountedCannon(BlockEntity target, ItemStack stack, boolean simulate) {
		PitchOrientedContraptionEntity contraptionEntity = null;
		if (target instanceof CompactCannonMountBlockEntity compactMount) {
			contraptionEntity = compactMount.getContraption();
		} else if (target instanceof ExtendsCannonMount mountExtension && mountExtension.getCannonMount() != null) {
			contraptionEntity = mountExtension.getCannonMount().getContraption();
		}
		if (contraptionEntity == null || !(contraptionEntity.getContraption() instanceof MountedBigCannonContraption cannon))
			return stack;
		return this.insertIntoBigCannon(stack, simulate, cannon, contraptionEntity);
	}

	private boolean tryEjectSpentCartridge() {
		int outputSlot = this.findEmptyOutputSlot();
		if (outputSlot < 0)
			return false;
		for (Direction direction : Direction.values()) {
			BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(direction));
			if (this.tryEjectSpentCartridge(target, outputSlot))
				return true;
		}
		return false;
	}

	private boolean tryEjectSpentCartridge(BlockEntity target, int outputSlot) {
		PitchOrientedContraptionEntity contraptionEntity = this.getMountedContraption(target);
		if (contraptionEntity == null || !(contraptionEntity.getContraption() instanceof MountedBigCannonContraption cannon))
			return false;
		BlockPos startPos = cannon.getStartPos();
		IBigCannonBlockEntity startCannon = getBigCannonBlockEntity(cannon, startPos);
		if (!(startCannon instanceof BlockEntity startCannonBlockEntity))
			return false;
		StructureTemplate.StructureBlockInfo current = ((BigCannonBehavior) startCannon.cannonBehavior()).block();
		StructureTemplate.StructureBlockInfo cannonInfo = cannon.getBlocks().get(startPos);
		if (!(current.state().getBlock() instanceof BigCartridgeBlock)
			|| BigCartridgeBlock.getPowerFromData(current) > 0 || cannonInfo == null)
			return false;
		ItemStack emptyCartridge = emptyBigCartridge();
		if (!isEmptyCartridge(emptyCartridge))
			return false;
		((BigCannonBehavior) startCannon.cannonBehavior()).removeBlock();
		BigCannonBlock.writeAndSyncSingleBlockData(startCannonBlockEntity, cannonInfo, (AbstractContraptionEntity) contraptionEntity, cannon);
		this.items[outputSlot] = emptyCartridge.copyWithCount(1);
		this.normalizePendingState();
		this.setChangedAndSync();
		return true;
	}

	@Nullable
	private PitchOrientedContraptionEntity getMountedContraption(BlockEntity target) {
		if (target instanceof CompactCannonMountBlockEntity compactMount)
			return compactMount.getContraption();
		if (target instanceof ExtendsCannonMount mountExtension && mountExtension.getCannonMount() != null)
			return mountExtension.getCannonMount().getContraption();
		return null;
	}

	private ItemStack insertIntoBigCannon(ItemStack stack, boolean simulate, MountedBigCannonContraption cannon,
										  PitchOrientedContraptionEntity contraptionEntity) {
		ItemStack quickfiringResult = CannonMountPoint.bigCannonInsert(stack, simulate, cannon, contraptionEntity);
		if (wasAccepted(stack, quickfiringResult))
			return quickfiringResult;
		BigCannonMunitionBlock munition = getMunitionBlock(stack);
		if (munition == null)
			return stack;
		if (munition instanceof ProjectileBlock)
			return this.insertProjectileIntoOrdinaryCannon(stack, simulate, munition, cannon, contraptionEntity);
		if (munition instanceof BigCartridgeBlock && BigCartridgeBlockItem.getPower(stack) > 0)
			return this.insertCartridgeIntoOrdinaryCannon(stack, simulate, munition, cannon, contraptionEntity);
		return stack;
	}

	private ItemStack insertProjectileIntoOrdinaryCannon(ItemStack stack, boolean simulate, BigCannonMunitionBlock munition,
														MountedBigCannonContraption cannon,
														PitchOrientedContraptionEntity contraptionEntity) {
		BlockPos startPos = cannon.getStartPos();
		if (!this.hasEmptyCannonSpaceAhead(cannon, startPos))
			return stack;
		IBigCannonBlockEntity startCannon = getBigCannonBlockEntity(cannon, startPos);
		if (startCannon == null)
			return stack;
		StructureTemplate.StructureBlockInfo current = ((BigCannonBehavior) startCannon.cannonBehavior()).block();
		if (!current.state().isAir()) {
			if (current.state().getBlock() instanceof BigCartridgeBlock
				&& BigCartridgeBlock.getPowerFromData(current) <= 0) {
				ItemStack emptyCartridge = emptyBigCartridge();
				if (!simulate)
					CannonMountPoint.loadProjectile(stack, munition, (AbstractContraptionEntity) contraptionEntity, cannon);
				return emptyCartridge;
			}
			return stack;
		}
		if (!simulate)
			CannonMountPoint.loadProjectile(stack, munition, (AbstractContraptionEntity) contraptionEntity, cannon);
		return consumeOne(stack);
	}

	private ItemStack insertCartridgeIntoOrdinaryCannon(ItemStack stack, boolean simulate, BigCannonMunitionBlock munition,
													   MountedBigCannonContraption cannon,
													   PitchOrientedContraptionEntity contraptionEntity) {
		BlockPos startPos = cannon.getStartPos();
		if (getBigCannonBlockEntity(cannon, startPos.relative(cannon.initialOrientation())) == null)
			return stack;
		IBigCannonBlockEntity startCannon = getBigCannonBlockEntity(cannon, startPos);
		if (startCannon == null)
			return stack;
		StructureTemplate.StructureBlockInfo current = ((BigCannonBehavior) startCannon.cannonBehavior()).block();
		if (!(current.state().getBlock() instanceof ProjectileBlock))
			return stack;
		if (!simulate)
			CannonMountPoint.loadCartridge(stack, munition, (AbstractContraptionEntity) contraptionEntity, cannon);
		return consumeOne(stack);
	}

	private boolean hasEmptyCannonSpaceAhead(MountedBigCannonContraption cannon, BlockPos startPos) {
		Direction direction = cannon.initialOrientation();
		BlockPos scanPos = startPos.relative(direction);
		boolean foundCannonBlock = false;
		while (getBigCannonBlockEntity(cannon, scanPos) instanceof IBigCannonBlockEntity cannonBlockEntity) {
			foundCannonBlock = true;
			StructureTemplate.StructureBlockInfo contained = ((BigCannonBehavior) cannonBlockEntity.cannonBehavior()).block();
			if (!contained.state().isAir())
				return false;
			scanPos = scanPos.relative(direction);
		}
		return foundCannonBlock;
	}

	@Nullable
	private static IBigCannonBlockEntity getBigCannonBlockEntity(MountedBigCannonContraption cannon, BlockPos pos) {
		BlockEntity blockEntity = cannon.presentBlockEntities.get(pos);
		return blockEntity instanceof IBigCannonBlockEntity bigCannon ? bigCannon : null;
	}

	@Nullable
	private static BigCannonMunitionBlock getMunitionBlock(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem))
			return null;
		Block block = blockItem.getBlock();
		return block instanceof BigCannonMunitionBlock munition ? munition : null;
	}

	private static ItemStack consumeOne(ItemStack stack) {
		ItemStack remainder = stack.copy();
		remainder.shrink(1);
		return remainder;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ListTag list = new ListTag();
		for (int slot = 0; slot < SLOT_COUNT; ++slot) {
			if (this.items[slot].isEmpty())
				continue;
			CompoundTag itemTag = new CompoundTag();
			itemTag.putByte("Slot", (byte) slot);
			itemTag.put("Item", this.items[slot].saveOptional(registries));
			list.add(itemTag);
		}
		tag.put("Items", list);
		tag.putBoolean("AutomaticLocked", this.automaticLocked);
		tag.putBoolean("AutomationFillStarted", this.automationFillStarted);
		tag.putInt("PendingCartridgeGroup", this.pendingCartridgeGroup);
		tag.putInt("PendingTargetDirection", this.pendingTargetDirection == null ? -1 : this.pendingTargetDirection.ordinal());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		for (int i = 0; i < SLOT_COUNT; ++i)
			this.items[i] = ItemStack.EMPTY;
		ListTag list = tag.getList("Items", 10);
		for (int i = 0; i < list.size(); ++i) {
			CompoundTag itemTag = list.getCompound(i);
			int slot = itemTag.getByte("Slot") & 255;
			if (slot < SLOT_COUNT)
				this.items[slot] = ItemStack.parseOptional(registries, itemTag.getCompound("Item"));
		}
		this.automaticLocked = tag.getBoolean("AutomaticLocked");
		this.automationFillStarted = tag.getBoolean("AutomationFillStarted");
		this.pendingCartridgeGroup = tag.contains("PendingCartridgeGroup") ? tag.getInt("PendingCartridgeGroup") : NO_PENDING_GROUP;
		int pendingDirection = tag.contains("PendingTargetDirection") ? tag.getInt("PendingTargetDirection") : -1;
		this.pendingTargetDirection = pendingDirection >= 0 && pendingDirection < Direction.values().length
			? Direction.values()[pendingDirection] : null;
		this.normalizePendingState();
	}

	private ItemStack insertIntoSlot(int slot, ItemStack stack, boolean simulate) {
		if (slot < 0 || slot >= SLOT_COUNT || stack.isEmpty() || !isValidForSlot(slot, stack) || !this.items[slot].isEmpty())
			return stack;
		ItemStack remainder = stack.copy();
		remainder.shrink(1);
		if (!simulate) {
			this.items[slot] = stack.copyWithCount(1);
			this.normalizePendingState();
			this.setChangedAndSync();
		}
		return remainder;
	}

	private int findAutomationSlot(ItemStack stack) {
		if (stack.isEmpty() || this.automaticLocked)
			return -1;
		if (isProjectile(stack)) {
			for (int slot = 0; slot < PROJECTILE_SLOTS; ++slot)
				if (this.items[slot].isEmpty())
					return slot;
			return -1;
		}
		if (isPropellant(stack)) {
			for (int group = 0; group < GROUP_COUNT; ++group) {
				int cartridgeSlot = cartridgeSlot(group);
				if (!this.items[group].isEmpty() && this.items[cartridgeSlot].isEmpty())
					return cartridgeSlot;
			}
		}
		return -1;
	}

	private void normalizePendingState() {
		if (this.pendingCartridgeGroup < 0 || this.pendingCartridgeGroup >= GROUP_COUNT
			|| this.items[cartridgeSlot(this.pendingCartridgeGroup)].isEmpty()) {
			this.pendingCartridgeGroup = NO_PENDING_GROUP;
			this.pendingTargetDirection = null;
		}
		if (this.isEmpty())
			this.automaticLocked = false;
		if (this.isEmpty())
			this.automationFillStarted = false;
	}

	private boolean isFullyLoaded() {
		for (int group = 0; group < GROUP_COUNT; ++group)
			if (!this.isGroupComplete(group))
				return false;
		return true;
	}

	private boolean isEmpty() {
		for (ItemStack stack : this.items)
			if (!stack.isEmpty())
				return false;
		return true;
	}

	private int findEmptySlot() {
		for (int slot = 0; slot < SLOT_COUNT; ++slot)
			if (this.items[slot].isEmpty())
				return slot;
		return -1;
	}

	private int findEmptyOutputSlot() {
		for (int slot = PROJECTILE_SLOTS; slot < SLOT_COUNT; ++slot)
			if (this.items[slot].isEmpty())
				return slot;
		return this.findEmptySlot();
	}

	private int findEmptyCartridgeSlot() {
		for (int slot = 0; slot < SLOT_COUNT; ++slot)
			if (isEmptyCartridge(this.items[slot]))
				return slot;
		return -1;
	}

	private int findCompleteGroup() {
		for (int group = 0; group < GROUP_COUNT; ++group)
			if (this.isGroupComplete(group))
				return group;
		return NO_PENDING_GROUP;
	}

	private boolean isGroupComplete(int group) {
		return isProjectile(this.items[group]) && isPropellant(this.items[cartridgeSlot(group)]);
	}

	private static int cartridgeSlot(int group) {
		return PROJECTILE_SLOTS + group;
	}

	private static boolean isProjectileSlot(int slot) {
		return slot >= 0 && slot < PROJECTILE_SLOTS;
	}

	private static boolean isCartridgeSlot(int slot) {
		return slot >= PROJECTILE_SLOTS && slot < SLOT_COUNT;
	}

	private static boolean isValidForSlot(int slot, ItemStack stack) {
		return isProjectileSlot(slot) && isProjectile(stack)
			|| isCartridgeSlot(slot) && isPropellant(stack);
	}

	private static boolean isProjectile(ItemStack stack) {
		return stack.getItem() instanceof ProjectileBlockItem
			|| stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ProjectileBlock;
	}

	private static boolean isPropellant(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem))
			return false;
		Block block = blockItem.getBlock();
		if (block instanceof BigCartridgeBlock)
			return stack.getItem() instanceof BigCartridgeBlockItem && BigCartridgeBlockItem.getPower(stack) > 0;
		return block instanceof BigCannonPropellantBlock;
	}

	private static boolean wasAccepted(ItemStack original, ItemStack remainder) {
		return !ItemStack.matches(remainder, original) || remainder.getCount() != original.getCount();
	}

	private static boolean isEmptyCartridge(ItemStack stack) {
		return stack.getItem() instanceof BigCartridgeBlockItem
			&& BigCartridgeBlockItem.getPower(stack) <= 0;
	}

	private static ItemStack emptyBigCartridge() {
		return BigCartridgeBlockItem.getWithPower(0);
	}

	private ItemStack extractEmptyCartridge(boolean simulate) {
		int slot = this.findEmptyCartridgeSlot();
		if (slot < 0)
			return ItemStack.EMPTY;
		ItemStack extracted = this.items[slot].copyWithCount(1);
		if (!simulate) {
			this.items[slot] = ItemStack.EMPTY;
			this.normalizePendingState();
			this.setChangedAndSync();
		}
		return extracted;
	}

	private class AutomationItemHandler implements IItemHandler {
		@Override public int getSlots() { return 1; }
		@Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return slot == 0 ? CannonMagazineLoaderBlockEntity.this.insertAutomation(stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override public int getSlotLimit(int slot) { return 64; }
		@Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && CannonMagazineLoaderBlockEntity.this.findAutomationSlot(stack) >= 0; }
	}

	private class ShellOutputAutomationItemHandler implements IItemHandler {
		@Override public int getSlots() { return 1; }

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot != 0)
				return ItemStack.EMPTY;
			int emptyCartridgeSlot = CannonMagazineLoaderBlockEntity.this.findEmptyCartridgeSlot();
			return emptyCartridgeSlot < 0 ? ItemStack.EMPTY : CannonMagazineLoaderBlockEntity.this.items[emptyCartridgeSlot];
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return slot == 0 ? CannonMagazineLoaderBlockEntity.this.insertAutomation(stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return slot == 0 && amount > 0 ? CannonMagazineLoaderBlockEntity.this.extractEmptyCartridge(simulate) : ItemStack.EMPTY;
		}

		@Override public int getSlotLimit(int slot) { return 64; }
		@Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && CannonMagazineLoaderBlockEntity.this.findAutomationSlot(stack) >= 0; }
	}

	private void setChangedAndSync() {
		this.setChanged();
		if (this.level != null)
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		this.saveAdditional(tag, registries);
		return tag;
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
