package com.cbcfirepowercomponents.compat.radar;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.mixin.radar.AutoPitchControllerAccessor;
import com.cbcfirepowercomponents.mixin.radar.AutoYawControllerAccessor;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class RadarCompactMountCompat {
	private static final String YAW_CONTROLLER = "com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity";

	private RadarCompactMountCompat() {
	}

	public static boolean trySelectCompactMount(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (!(level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity))
			return false;

		if (!level.isClientSide) {
			ItemStack stack = context.getItemInHand();
			CompoundTag tag = getLinkTag(stack);
			tag.put("SelectedMountPos", NbtUtils.writeBlockPos(pos));
			tag.remove("SelectedFiltererPos");
			tag.remove("SelectedYawPos");
			tag.remove("SelectedPitchPos");
			tag.remove("SelectedFiringPos");
			setLinkTag(stack, tag);
			if (context.getPlayer() != null)
				context.getPlayer().displayClientMessage(Component.translatable("create_radar.data_link.mount_set"), true);
		}
		return true;
	}

	public static boolean hasCompactYawMount(BlockEntity controller) {
		return findYawMount(controller) != null;
	}

	public static boolean hasCompactPitchMount(BlockEntity controller) {
		return findPitchMount(controller) != null;
	}

	public static boolean tickYaw(BlockEntity controller) {
		CompactCannonMountBlockEntity mount = findYawMount(controller);
		if (mount == null)
			return false;
		if (controller.getLevel() == null || controller.getLevel().isClientSide)
			return true;

		AutoYawControllerAccessor access = (AutoYawControllerAccessor) controller;
		if (!access.cbcfpc$isRunningController())
			return true;
		if (mount.getContraption() == null)
			return true;

		double current = wrap360(mount.getCannonYaw());
		double target = wrap360(access.cbcfpc$getTargetAngle());
		double diff = shortestDelta(current, target);
		double tolerance = AutoYawControllerAccessor.cbcfpc$getToleranceDeg();
		double next;
		if (Math.abs(diff) <= tolerance) {
			next = target;
		} else {
			double step = Math.abs(((KineticBlockEntity) controller).getSpeed()) / 24.0d;
			if (step <= 0)
				return true;
			next = current + Math.signum(diff) * Math.min(Math.abs(diff), step);
		}

		next = wrap360(next);
		mount.setLimitedYaw((float) next);
		access.cbcfpc$recordCbcYawWritten(next);
		return true;
	}

	public static boolean tickPitch(BlockEntity controller) {
		CompactCannonMountBlockEntity mount = findPitchMount(controller);
		if (mount == null)
			return false;
		if (controller.getLevel() == null || controller.getLevel().isClientSide)
			return true;

		AutoPitchControllerAccessor access = (AutoPitchControllerAccessor) controller;
		if (!access.cbcfpc$isRunningController())
			return true;
		if (mount.getContraption() == null)
			return true;

		double current = mount.getCannonPitch();
		double target = clampPitch(access.cbcfpc$getTargetAngle());
		double diff = target - current;
		double tolerance = AutoPitchControllerAccessor.cbcfpc$getCbcTolerance();
		double next;
		if (Math.abs(diff) <= tolerance) {
			next = target;
		} else {
			double step = Math.abs(((KineticBlockEntity) controller).getSpeed()) / 24.0d;
			if (step <= 0)
				return true;
			next = current + Math.signum(diff) * Math.min(Math.abs(diff), step);
		}

		mount.setLimitedPitch((float) clampPitch(next));
		return true;
	}

	public static boolean setYawTarget(BlockEntity controller, Vec3 target) {
		if (findYawMount(controller) == null)
			return false;
		AutoYawControllerAccessor access = (AutoYawControllerAccessor) controller;
		Vec3 origin = access.cbcfpc$isUpsideDown()
			? controller.getBlockPos().below(3).getCenter()
			: controller.getBlockPos().above(3).getCenter();
		double yaw = wrap360(access.cbcfpc$computeYawToTargetDeg(origin, target)) + 180.0d;
		return setYawTargetAngle(controller, yaw);
	}

	public static boolean setYawTargetAngle(BlockEntity controller, double angle) {
		if (findYawMount(controller) == null)
			return false;
		AutoYawControllerAccessor access = (AutoYawControllerAccessor) controller;
		access.cbcfpc$setInternalTargetAngle(wrap360(angle));
		access.cbcfpc$setRunning(true);
		updateController(controller);
		return true;
	}

	public static boolean setPitchTarget(BlockEntity controller, Vec3 target) {
		CompactCannonMountBlockEntity mount = findPitchMount(controller);
		if (mount == null)
			return false;
		AutoPitchControllerAccessor access = (AutoPitchControllerAccessor) controller;
		Vec3 origin = access.cbcfpc$getRayStart();
		if (origin == null)
			origin = controller.getBlockPos().getCenter();
		double horizontal = Math.sqrt(target.distanceToSqr(origin.x, target.y, origin.z));
		double pitch = Math.toDegrees(Math.atan2(target.y - origin.y, horizontal));
		access.cbcfpc$setInternalTargetAngle(clampPitch(pitch));
		access.cbcfpc$setLastTargetPos(target);
		access.cbcfpc$setRunning(true);
		updateController(controller);

		BlockEntity yawController = findYawControllerNear(mount);
		if (yawController != null)
			setYawTarget(yawController, target);
		return true;
	}

	public static boolean setPitchTargetAngle(BlockEntity controller, double angle) {
		if (findPitchMount(controller) == null)
			return false;
		AutoPitchControllerAccessor access = (AutoPitchControllerAccessor) controller;
		access.cbcfpc$setInternalTargetAngle(clampPitch(angle));
		access.cbcfpc$setRunning(true);
		updateController(controller);
		return true;
	}

	public static boolean setPitchTrackTarget(BlockEntity controller, @Nullable Object track) {
		if (findPitchMount(controller) == null)
			return false;
		if (track == null) {
			((AutoPitchControllerAccessor) controller).cbcfpc$setRunning(false);
			updateController(controller);
			return true;
		}
		Vec3 target = readTrackPosition(track);
		return target != null && setPitchTarget(controller, target);
	}

	public static boolean setPitchBlockTarget(BlockEntity controller, @Nullable BlockPos pos, boolean active) {
		if (findPitchMount(controller) == null)
			return false;
		if (!active || pos == null) {
			((AutoPitchControllerAccessor) controller).cbcfpc$setRunning(false);
			updateController(controller);
			return true;
		}
		return setPitchTarget(controller, pos.getCenter());
	}

	public static boolean atTargetYaw(BlockEntity controller) {
		CompactCannonMountBlockEntity mount = findYawMount(controller);
		if (mount == null || mount.getContraption() == null)
			return false;
		double diff = shortestDelta(wrap360(mount.getCannonYaw()), wrap360(((AutoYawControllerAccessor) controller).cbcfpc$getTargetAngle()));
		return Math.abs(diff) <= AutoYawControllerAccessor.cbcfpc$getToleranceDeg();
	}

	public static boolean atTargetPitch(BlockEntity controller) {
		CompactCannonMountBlockEntity mount = findPitchMount(controller);
		if (mount == null || mount.getContraption() == null)
			return false;
		double diff = clampPitch(((AutoPitchControllerAccessor) controller).cbcfpc$getTargetAngle()) - mount.getCannonPitch();
		return Math.abs(diff) <= AutoPitchControllerAccessor.cbcfpc$getCbcTolerance();
	}

	@Nullable
	private static CompactCannonMountBlockEntity findYawMount(BlockEntity controller) {
		Level level = controller.getLevel();
		if (level == null)
			return null;
		BlockPos first = controller.getBlockPos().above();
		BlockPos second = controller.getBlockPos().below();
		if (controller instanceof AutoYawControllerAccessor access && access.cbcfpc$isUpsideDown()) {
			first = controller.getBlockPos().below();
			second = controller.getBlockPos().above();
		}
		CompactCannonMountBlockEntity mount = compactMountAt(level, first);
		return mount != null ? mount : compactMountAt(level, second);
	}

	@Nullable
	private static CompactCannonMountBlockEntity findPitchMount(BlockEntity controller) {
		Level level = controller.getLevel();
		if (level == null)
			return null;
		CompactCannonMountBlockEntity found = null;
		for (Direction direction : Direction.values()) {
			CompactCannonMountBlockEntity mount = compactMountAt(level, controller.getBlockPos().relative(direction));
			if (mount == null)
				continue;
			if (found != null)
				return found;
			found = mount;
		}
		return found;
	}

	@Nullable
	private static CompactCannonMountBlockEntity compactMountAt(Level level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount ? mount : null;
	}

	@Nullable
	private static BlockEntity findYawControllerNear(CompactCannonMountBlockEntity mount) {
		Level level = mount.getLevel();
		if (level == null)
			return null;
		for (Direction direction : Direction.values()) {
			BlockEntity be = level.getBlockEntity(mount.getBlockPos().relative(direction));
			if (be != null && YAW_CONTROLLER.equals(be.getClass().getName()))
				return be;
		}
		return null;
	}

	@Nullable
	private static Vec3 readTrackPosition(Object track) {
		try {
			Method method = track.getClass().getMethod("getPosition");
			Object result = method.invoke(track);
			return result instanceof Vec3 vec ? vec : null;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static void updateController(BlockEntity controller) {
		controller.setChanged();
		if (controller instanceof SmartBlockEntity smart)
			smart.notifyUpdate();
	}

	private static double clampPitch(double pitch) {
		return Math.max(-90.0d, Math.min(90.0d, pitch));
	}

	private static double wrap360(double angle) {
		double result = angle % 360.0d;
		if (result < 0.0d)
			result += 360.0d;
		return result;
	}

	private static double shortestDelta(double current, double target) {
		double delta = (target - current) % 360.0d;
		if (delta > 180.0d)
			delta -= 360.0d;
		if (delta < -180.0d)
			delta += 360.0d;
		return delta;
	}

	private static CompoundTag getLinkTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static void setLinkTag(ItemStack stack, CompoundTag tag) {
		if (tag == null || tag.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
			return;
		}
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
	}
}