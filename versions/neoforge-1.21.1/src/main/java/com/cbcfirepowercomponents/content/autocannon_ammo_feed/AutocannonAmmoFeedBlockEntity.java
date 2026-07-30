package com.cbcfirepowercomponents.content.autocannon_ammo_feed;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.large_autocannon_ammo_box.LargeAutocannonAmmoBoxCapacity;
import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;

public class AutocannonAmmoFeedBlockEntity extends AutocannonAmmoContainerBlockEntity {
	private static final int CAPACITY = 64;

	public AutocannonAmmoFeedBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.AUTOCANNON_AMMO_FEED.get(), pos, state);
	}

	@Override
	public int getMainAmmoCapacity() {
		return CAPACITY;
	}

	@Override
	public int getTracerAmmoCapacity() {
		return CAPACITY;
	}

	@Override
	public int getTotalCount() {
		return this.getMainAmmoStack().getCount() + this.getTracerStack().getCount();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return LargeAutocannonAmmoBoxCapacity.canPlace(this, slot, stack);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.cbc_firepower_components.autocannon_ammo_feed");
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AutocannonAmmoFeedBlockEntity feed) {
		int slot = feed.getNextOutputSlot();
		if (slot < 0)
			return;

		ItemStack offered = (slot == 1 ? feed.getTracerStack() : feed.getMainAmmoStack()).copyWithCount(1);
		for (Direction direction : Direction.values()) {
			BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
			if (!(blockEntity instanceof CompactCannonMountBlockEntity mount))
				continue;
			ItemStack remainder = mount.insertAutocannonFeedAmmo(offered, false);
			if (!ItemStack.matches(remainder, offered) || remainder.getCount() != offered.getCount()) {
				feed.removeItem(slot, 1);
				feed.setChanged();
				return;
			}
		}
	}

	private int getNextOutputSlot() {
		ItemStack main = this.getMainAmmoStack();
		ItemStack tracers = this.getTracerStack();
		if (main.isEmpty())
			return tracers.isEmpty() ? -1 : 1;
		if (tracers.isEmpty())
			return 0;

		int cycle = Math.max(1, this.getSpacing() + 1);
		return this.getTotalCount() % cycle == 0 ? 1 : 0;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (this.getTotalCount() == 0 && tag.contains("Ammo"))
			this.setMainAmmoDirect(ItemStack.parseOptional(registries, tag.getCompound("Ammo")));
	}
}
