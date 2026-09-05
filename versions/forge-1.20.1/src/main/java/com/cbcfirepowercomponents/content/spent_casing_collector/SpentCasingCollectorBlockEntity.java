package com.cbcfirepowercomponents.content.spent_casing_collector;

import static com.cbcfirepowercomponents.content.CannonAmmunitionHelper.*;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class SpentCasingCollectorBlockEntity extends BlockEntity {
	private static final int SLOT_COUNT = 4;
	public static final double HORIZONTAL_COLLECTION_RANGE = 6.0;
	public static final double VERTICAL_COLLECTION_RANGE = 3.0;
	private final ItemStack[] items = new ItemStack[SLOT_COUNT];
	private final IItemHandler handler = new CasingHandler();
	private final LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(() -> this.handler);
	private int cooldown;

	public SpentCasingCollectorBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.SPENT_CASING_COLLECTOR.get(), pos, state);
		for (int i = 0; i < SLOT_COUNT; ++i)
			this.items[i] = ItemStack.EMPTY;
	}

	public IItemHandler getItemHandler(@Nullable Direction side) {
		return this.handler;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
		return capability == ForgeCapabilities.ITEM_HANDLER
			? this.itemHandlerCapability.cast() : super.getCapability(capability, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.itemHandlerCapability.invalidate();
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SpentCasingCollectorBlockEntity collector) {
		collector.updateFillState();
		if (collector.cooldown-- > 0)
			return;
		collector.cooldown = 4;
		collector.collectNearbyCasings();
	}

	private void collectNearbyCasings() {
		if (this.level == null)
			return;
		AABB collectionArea = new AABB(this.worldPosition)
			.inflate(HORIZONTAL_COLLECTION_RANGE, VERTICAL_COLLECTION_RANGE, HORIZONTAL_COLLECTION_RANGE);
		for (ItemEntity entity : this.level.getEntitiesOfClass(ItemEntity.class,
			collectionArea, item -> item.isAlive() && isSpentCasing(item.getItem()))) {
			ItemStack original = entity.getItem();
			ItemStack remainder = this.insert(original, false);
			if (remainder.getCount() == original.getCount())
				continue;
			if (remainder.isEmpty())
				entity.discard();
			else
				entity.setItem(remainder);
			if (!this.hasRoom())
				break;
		}
	}

	private boolean hasRoom() {
		for (ItemStack stack : this.items)
			if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize())
				return true;
		return false;
	}

	private ItemStack insert(ItemStack stack, boolean simulate) {
		if (!isSpentCasing(stack))
			return stack;
		int slot = this.findInsertSlot(stack);
		if (slot < 0)
			return stack;
		int room = this.items[slot].isEmpty() ? stack.getMaxStackSize()
			: this.items[slot].getMaxStackSize() - this.items[slot].getCount();
		int accepted = Math.min(room, stack.getCount());
		if (accepted <= 0)
			return stack;
		ItemStack remainder = stack.copy();
		remainder.shrink(accepted);
		if (!simulate) {
			if (this.items[slot].isEmpty())
				this.items[slot] = stack.copyWithCount(accepted);
			else
				this.items[slot].grow(accepted);
			this.setChangedAndSync();
		}
		return remainder;
	}

	private int findInsertSlot(ItemStack stack) {
		for (int i = 0; i < SLOT_COUNT; ++i)
			if (!this.items[i].isEmpty() && ItemStack.isSameItemSameTags(this.items[i], stack)
				&& this.items[i].getCount() < this.items[i].getMaxStackSize())
				return i;
		for (int i = 0; i < SLOT_COUNT; ++i)
			if (this.items[i].isEmpty())
				return i;
		return -1;
	}

	public ItemStack extractOne(boolean simulate) {
		for (int i = 0; i < SLOT_COUNT; ++i) {
			if (this.items[i].isEmpty())
				continue;
			ItemStack result = this.items[i].copyWithCount(1);
			if (!simulate) {
				this.items[i].shrink(1);
				if (this.items[i].isEmpty())
					this.items[i] = ItemStack.EMPTY;
				this.setChangedAndSync();
			}
			return result;
		}
		return ItemStack.EMPTY;
	}

	public void dropContents(Level level) {
		for (int i = 0; i < SLOT_COUNT; ++i) {
			if (!this.items[i].isEmpty())
				Containers.dropItemStack(level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.items[i]);
			this.items[i] = ItemStack.EMPTY;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ListTag list = new ListTag();
		for (int i = 0; i < SLOT_COUNT; ++i) {
			if (this.items[i].isEmpty()) continue;
			CompoundTag entry = new CompoundTag();
			entry.putByte("Slot", (byte) i);
			entry.put("Item", this.items[i].save(new CompoundTag()));
			list.add(entry);
		}
		tag.put("Items", list);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		for (int i = 0; i < SLOT_COUNT; ++i) this.items[i] = ItemStack.EMPTY;
		ListTag list = tag.getList("Items", 10);
		for (int i = 0; i < list.size(); ++i) {
			CompoundTag entry = list.getCompound(i);
			int slot = entry.getByte("Slot") & 255;
			if (slot < SLOT_COUNT) this.items[slot] = ItemStack.of(entry.getCompound("Item"));
		}
	}

	private void setChangedAndSync() {
		this.setChanged();
		this.updateFillState();
		if (this.level != null)
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
	}

	private void updateFillState() {
		if (this.level == null || this.level.isClientSide)
			return;
		int count = 0;
		for (ItemStack stack : this.items)
			count += stack.getCount();
		int fullThreshold = SLOT_COUNT * 64 * 3 / 4;
		int fill = count == 0 ? 0 : count >= fullThreshold ? 2 : 1;
		BlockState state = this.getBlockState();
		if (state.hasProperty(SpentCasingCollectorBlock.FILL) && state.getValue(SpentCasingCollectorBlock.FILL) != fill)
			this.level.setBlock(this.worldPosition, state.setValue(SpentCasingCollectorBlock.FILL, fill), 3);
	}

	@Override public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		this.saveAdditional(tag);
		return tag;
	}
	@Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private class CasingHandler implements IItemHandler {
		@Override public int getSlots() { return SLOT_COUNT; }
		@Override public ItemStack getStackInSlot(int slot) {
			return slot >= 0 && slot < SLOT_COUNT ? items[slot] : ItemStack.EMPTY;
		}
		@Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return SpentCasingCollectorBlockEntity.this.insert(stack, simulate);
		}
		@Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot < 0 || slot >= SLOT_COUNT || amount <= 0 || items[slot].isEmpty())
				return ItemStack.EMPTY;
			int count = Math.min(amount, items[slot].getCount());
			ItemStack result = items[slot].copyWithCount(count);
			if (!simulate) {
				items[slot].shrink(count);
				if (items[slot].isEmpty()) items[slot] = ItemStack.EMPTY;
				setChangedAndSync();
			}
			return result;
		}
		@Override public int getSlotLimit(int slot) { return 64; }
		@Override public boolean isItemValid(int slot, ItemStack stack) { return isSpentCasing(stack); }
	}
}
