package com.cbcfirepowercomponents.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CleanBlockItem extends BlockItem {
	public CleanBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public void verifyTagAfterLoad(CompoundTag tag) {
		super.verifyTagAfterLoad(tag);
		tag.remove("BlockStateTag");
		tag.remove("BlockEntityTag");
	}
}
