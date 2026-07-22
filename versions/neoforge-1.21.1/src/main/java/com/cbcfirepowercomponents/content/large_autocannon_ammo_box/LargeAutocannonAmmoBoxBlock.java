package com.cbcfirepowercomponents.content.large_autocannon_ammo_box;

import com.cbcfirepowercomponents.registry.MTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlock;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerBlockEntity;

public class LargeAutocannonAmmoBoxBlock extends AutocannonAmmoContainerBlock {
	private static final VoxelShape SHAPE = box(1, 0, 2, 15, 16, 14);

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

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}