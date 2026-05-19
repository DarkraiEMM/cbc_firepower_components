package com.cbcfirepowercomponents.content.machine_gun_shield;

import com.cbcfirepowercomponents.registry.MTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SleeveMachineGunShieldItem extends BlockItem {

	public SleeveMachineGunShieldItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos barrelPos = context.getClickedPos();
		BlockState barrelState = level.getBlockState(barrelPos);
		Direction.Axis barrelAxis = getBarrelAxis(barrelState);
		Player player = context.getPlayer();
		if (player == null || !isAttachTarget(barrelState))
			return super.useOn(context);

		Direction facing = getAttachmentFace(context, barrelPos, player, barrelAxis);
		BlockPos shieldPos = barrelPos.relative(facing);
		if (!level.getBlockState(shieldPos).isAir())
			return InteractionResult.FAIL;

		BlockState shieldState = MTBlocks.SLEEVE_MACHINE_GUN_SHIELD.get()
			.defaultBlockState()
			.setValue(SleeveMachineGunShieldBlock.FACING, facing);

		if (!level.setBlock(shieldPos, shieldState, 11))
			return InteractionResult.FAIL;

		ItemStack stack = context.getItemInHand();
		if (!player.getAbilities().instabuild)
			stack.shrink(1);

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	private static boolean isAttachTarget(BlockState state) {
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return "createbigcannons".equals(id.getNamespace()) && (id.getPath().contains("cannon") || id.getPath().contains("barrel"));
	}

	private static Direction.Axis getBarrelAxis(BlockState state) {
		if (state.hasProperty(BlockStateProperties.AXIS))
			return state.getValue(BlockStateProperties.AXIS);
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			return state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		if (state.hasProperty(BlockStateProperties.FACING))
			return state.getValue(BlockStateProperties.FACING).getAxis();
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
		return null;
	}

	private static Direction getAttachmentFace(UseOnContext context, BlockPos pos, Player player, Direction.Axis axis) {
		Direction clickedFace = context.getClickedFace();
		if (clickedFace.getAxis().isHorizontal())
			return clickedFace;
		if (axis == Direction.Axis.X)
			return player.getX() >= pos.getX() + 0.5D ? Direction.EAST : Direction.WEST;
		if (axis == Direction.Axis.Z)
			return player.getZ() >= pos.getZ() + 0.5D ? Direction.SOUTH : Direction.NORTH;
		return player.getDirection().getOpposite();
	}

}
