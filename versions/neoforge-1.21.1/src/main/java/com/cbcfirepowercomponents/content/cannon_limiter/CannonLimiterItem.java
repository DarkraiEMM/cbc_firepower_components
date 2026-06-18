package com.cbcfirepowercomponents.content.cannon_limiter;

import java.util.List;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CannonLimiterItem extends Item {
	public CannonLimiterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide)
			openClientConfig(hand, stack.copy());
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof CompactCannonMountBlockEntity mount)) {
			if (player != null && !level.isClientSide)
				show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.invalid"));
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		if (player == null)
			return InteractionResult.sidedSuccess(level.isClientSide);

		if (!level.isClientSide) {
			if (player.isShiftKeyDown()) {
				boolean hadLimiter = mount.hasLimiter();
				ItemStack removed = mount.removeLimiter();
				if (!hadLimiter) {
					show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.no_limiter"));
				} else {
					if (!removed.isEmpty() && !player.getAbilities().instabuild && !player.getInventory().add(removed))
						player.drop(removed, false);
					show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.removed"));
				}
			} else {
				ItemStack stack = context.getItemInHand();
				CannonLimiterSettings settings = CannonLimiterSettings.get(stack);
				if (!settings.hasAnyLimit()) {
					show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.empty"));
				} else if (mount.hasLimiter()) {
					show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.occupied"));
				} else {
					ItemStack installed = stack.copy();
					installed.setCount(1);
					mount.installLimiter(installed);
					if (!player.getAbilities().instabuild)
						stack.shrink(1);
					show(player, Component.translatable("item.cbc_firepower_components.cannon_limiter.message.installed"));
				}
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		CannonLimiterSettings settings = CannonLimiterSettings.get(stack);
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.summary")
			.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.use")
			.withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.install")
			.withStyle(ChatFormatting.DARK_AQUA));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.pitch_min",
				settings.format(settings.hasPitchMin, settings.pitchMin))
			.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.pitch_max",
				settings.format(settings.hasPitchMax, settings.pitchMax))
			.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.yaw_min",
				settings.format(settings.hasYawMin, settings.yawMin))
			.withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.translatable("item.cbc_firepower_components.cannon_limiter.tooltip.yaw_max",
				settings.format(settings.hasYawMax, settings.yawMax))
			.withStyle(ChatFormatting.YELLOW));
	}

	private static void show(Player player, Component message) {
		player.displayClientMessage(message, true);
	}

	private static void openClientConfig(InteractionHand hand, ItemStack stack) {
		try {
			Class<?> client = Class.forName("com.cbcfirepowercomponents.client.CannonLimiterClient");
			client.getMethod("openConfig", InteractionHand.class, ItemStack.class).invoke(null, hand, stack);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
