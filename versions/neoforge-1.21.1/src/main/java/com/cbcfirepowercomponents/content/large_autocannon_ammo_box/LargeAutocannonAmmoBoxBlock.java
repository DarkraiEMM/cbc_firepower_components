package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlock;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;

public class LargeAutocannonAmmoBoxBlock extends AutocannonAmmoContainerBlock {
	public LargeAutocannonAmmoBoxBlock(Properties properties) {
		super(properties);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<AutocannonAmmoContainerBlockEntity> getBlockEntityClass() {
		return (Class<AutocannonAmmoContainerBlockEntity>) (Class<?>) LargeAutocannonAmmoBoxBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends AutocannonAmmoContainerBlockEntity> getBlockEntityType() {
		return MTBlockEntities.LARGE_AUTOCANNON_AMMO_BOX.get();
	}
}