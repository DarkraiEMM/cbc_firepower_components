package com.cbcfirepowercomponents.content.compact_cannon_mount;

import static net.minecraft.ChatFormatting.GRAY;
import static rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity.cannonBlockOutsideOfWorld;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterSettings;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.base.multiple_kinetic_interface.HasMultipleKineticInterfaces;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.ExtendsCannonMount;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;
import rbasamoyai.createbigcannons.munitions.autocannon.ammo_container.AutocannonAmmoContainerItem;

public class CompactCannonMountBlockEntity extends SmartBlockEntity implements IDisplayAssemblyExceptions,
	ControlPitchContraption.Block, HasMultipleKineticInterfaces, IHaveGoggleInformation {

	public enum LimitType {
		PITCH_MIN,
		PITCH_MAX,
		YAW_MIN,
		YAW_MAX
	}

	private AssemblyException lastException = null;
	protected PitchOrientedContraptionEntity mountedContraption;
	private boolean running;
	private boolean reassemble;
	private boolean hasPitchMin;
	private boolean hasPitchMax;
	private boolean hasYawMin;
	private boolean hasYawMax;
	private float pitchMin;
	private float pitchMax;
	private float yawMin;
	private float yawMax;
	private ItemStack cannonLimiter = ItemStack.EMPTY;

	private float cannonYaw;
	private float cannonPitch;
	private float prevYaw;
	private float prevPitch;
	private float clientYawDiff;
	private float clientPitchDiff;

	protected final CompactCannonMountInterfaceBlockEntity pitchInterface;
	protected final CompactCannonMountInterfaceBlockEntity yawInterface;
	private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new MountItemHandler());

	public CompactCannonMountBlockEntity(BlockPos pos, BlockState state) {
		this(MTBlockEntities.COMPACT_CANNON_MOUNT.get(), pos, state);
	}

	public CompactCannonMountBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.cannonYaw = state.getValue(CompactCannonMountBlock.HORIZONTAL_FACING).toYRot();
		this.setLazyTickRate(3);
		this.pitchInterface = new CompactCannonMountInterfaceBlockEntity.PitchInterface(type, pos, state, this);
		this.yawInterface = new CompactCannonMountInterfaceBlockEntity.YawInterface(type, pos, state, this);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		this.pitchInterface.setLevel(level);
		this.yawInterface.setLevel(level);
	}

	@Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override public BlockState getControllerState() { return this.getBlockState(); }

	@Nullable
	@Override
	public ResourceLocation getTypeId() {
		return new ResourceLocation(FirepowerComponents.MOD_ID, "compact_cannon_mount");
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(this.getBlockPos()).expandTowards(Vec3.atLowerCornerOf(this.getCannonSide().getNormal()));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.reassemble && this.getLevel() instanceof ServerLevel serverLevel) {
			this.getBlockState().tick(serverLevel, this.worldPosition, serverLevel.getRandom());
			this.reassemble = false;
		}
		this.pitchInterface.tick();
		this.yawInterface.tick();

		if (this.mountedContraption != null && !this.mountedContraption.isAlive())
			this.mountedContraption = null;

		this.prevYaw = this.cannonYaw;
		this.prevPitch = this.cannonPitch;

		boolean canTurn = this.mountedContraption != null && this.mountedContraption.canBeTurnedByController(this);
		if (this.getLevel().isClientSide) {
			this.clientYawDiff = canTurn ? this.clientYawDiff * 0.5f : 0;
			this.clientPitchDiff = canTurn ? this.clientPitchDiff * 0.5f : 0;
		}

		if (!this.running && !this.isVirtual()) {
			this.cannonYaw = this.getBlockState().getValue(CompactCannonMountBlock.HORIZONTAL_FACING).toYRot();
			this.prevYaw = this.cannonYaw;
			this.cannonPitch = 0;
			this.prevPitch = 0;
			return;
		}

		if (!(this.mountedContraption != null && this.mountedContraption.isStalled()) && canTurn) {
			Direction dir = this.mountedContraption.getInitialOrientation();
			boolean positive = (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) == (dir.getAxis() == Direction.Axis.X);
			float sign = positive ? 1 : -1;

			float yawSpeed = this.getAngularSpeed(-this.yawInterface.getSpeed(), this.clientYawDiff);
			float pitchSpeed = this.getAngularSpeed(this.pitchInterface.getSpeed(), this.clientPitchDiff * sign);

			double yawAngleLimit = this.yawInterface.getSequencedAngleLimit();
			if (yawAngleLimit >= 0) {
				yawSpeed = (float) Mth.clamp(yawSpeed, -yawAngleLimit, yawAngleLimit);
				this.yawInterface.setSequencedAngleLimit(Math.max(0, yawAngleLimit - Math.abs(yawSpeed)));
			}

			double pitchAngleLimit = this.pitchInterface.getSequencedAngleLimit();
			if (pitchAngleLimit >= 0) {
				pitchSpeed = (float) Mth.clamp(pitchSpeed, -pitchAngleLimit, pitchAngleLimit);
				this.pitchInterface.setSequencedAngleLimit(Math.max(0, pitchAngleLimit - Math.abs(pitchSpeed)));
			}

			this.cannonYaw = this.clampYawToLimits(this.cannonYaw + yawSpeed);
			float newPitch = this.cannonPitch + pitchSpeed * sign;
			this.cannonPitch = this.clampPitchToLimits(Mth.clamp(newPitch % 360.0f, -this.getMaxDepress(), this.getMaxElevate()));
		}
		this.applyRotation();
	}

	private float getMaxDepress() {
		float depression = this.mountedContraption.maximumDepression();
		float elevation = this.mountedContraption.maximumElevation();
		return depression == 0 && elevation == 0 ? 90 : depression;
	}

	private float getMaxElevate() {
		float depression = this.mountedContraption.maximumDepression();
		float elevation = this.mountedContraption.maximumElevation();
		return depression == 0 && elevation == 0 ? 90 : elevation;
	}

	protected void applyRotation() {
		if (this.mountedContraption == null)
			return;

		Direction dir = this.mountedContraption.getInitialOrientation();
		boolean positive = (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) == (dir.getAxis() == Direction.Axis.X);
		float sign = positive ? 1 : -1;

		if (!this.mountedContraption.canBeTurnedByController(this)) {
			float minPitch = -this.mountedContraption.maximumDepression();
			float maxPitch = this.mountedContraption.maximumElevation();
			this.cannonPitch = this.clampPitchToLimits(Mth.clamp(this.mountedContraption.pitch, minPitch, maxPitch) * sign);
			this.cannonYaw = this.clampYawToLimits(this.mountedContraption.yaw);
		} else {
			this.mountedContraption.pitch = this.cannonPitch * sign;
			this.mountedContraption.yaw = this.cannonYaw;
		}
	}

	public void onRedstoneUpdate(boolean assemblyPowered, boolean prevAssemblyPowered, boolean firePowered, boolean prevFirePowered, int firePower) {
		if (assemblyPowered != prevAssemblyPowered || this.reassemble) {
			this.getLevel().setBlock(this.worldPosition, this.getBlockState().setValue(CompactCannonMountBlock.ASSEMBLY_POWERED, assemblyPowered), 3);
			if (assemblyPowered) {
				try {
					this.assemble();
					this.lastException = null;
				} catch (AssemblyException e) {
					this.lastException = e;
					this.sendData();
				}
			} else {
				this.disassemble();
				this.sendData();
			}
		}
		if (firePowered != prevFirePowered)
			this.getLevel().setBlock(this.worldPosition, this.getBlockState().setValue(CompactCannonMountBlock.FIRE_POWERED, firePowered), 3);

		if (this.running && this.mountedContraption != null && this.getLevel() instanceof ServerLevel serverLevel) {
			((AbstractMountedCannonContraption) this.mountedContraption.getContraption()).onRedstoneUpdate(serverLevel, this.mountedContraption, firePowered != prevFirePowered, firePower, this);
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (this.running && this.mountedContraption != null)
			this.sendData();
	}

	public float getPitchOffset(float partialTicks) {
		float downSign = this.mountedContraption != null && this.mountedContraption.getInitialOrientation() == Direction.DOWN ? -1 : 1;
		Direction facing = this.getContraptionDirection();
		boolean positive = (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) == (facing.getAxis() == Direction.Axis.X);
		float horizontalSign = positive ? 1 : -1;
		if (this.isVirtual())
			return Mth.lerp(partialTicks + 0.5f, this.prevPitch, this.cannonPitch) * downSign;
		if (this.mountedContraption == null || this.mountedContraption.isStalled() || !this.running)
			partialTicks = 0;
		if (this.mountedContraption != null && !this.mountedContraption.canBeTurnedByController(this))
			return this.mountedContraption.getViewXRot(partialTicks + 1) * horizontalSign * downSign;
		float speed = this.getAngularSpeed(this.pitchInterface.getSpeed(), this.clientPitchDiff * horizontalSign);
		double pitchLimit = this.pitchInterface.getSequencedAngleLimit();
		if (pitchLimit >= 0)
			speed = (float) Mth.clamp(speed, -pitchLimit, pitchLimit);
		float pitchOffset = Mth.lerp(partialTicks, this.cannonPitch, this.clampPitchToLimits(this.cannonPitch + speed * horizontalSign));
		if (this.mountedContraption != null) {
			float minPitch = -this.getMaxDepress();
			float maxPitch = this.getMaxElevate();
			if (this.cannonPitch <= minPitch) {
				pitchOffset = minPitch;
			} else if (maxPitch <= this.cannonPitch) {
				pitchOffset = maxPitch;
			} else {
				pitchOffset = Mth.clamp(pitchOffset % 360.0f, minPitch, maxPitch);
			}
		}
		return pitchOffset * downSign;
	}

	public float getYawOffset(float partialTicks) {
		if (this.isVirtual())
			return Mth.lerp(partialTicks + 0.5f, this.prevYaw, this.cannonYaw);
		if (this.mountedContraption == null || this.mountedContraption.isStalled() || !this.running)
			partialTicks = 0;
		if (this.mountedContraption != null && !this.mountedContraption.canBeTurnedByController(this))
			return -this.mountedContraption.getViewYRot(partialTicks + 1);
		float speed = this.getAngularSpeed(-this.yawInterface.getSpeed(), this.clientYawDiff);
		double yawLimit = this.yawInterface.getSequencedAngleLimit();
		if (yawLimit >= 0)
			speed = (float) Mth.clamp(speed, -yawLimit, yawLimit);
		return Mth.lerp(partialTicks, this.cannonYaw, this.clampYawToLimits(this.cannonYaw + speed));
	}

	public float getDisplayPitch() {
		return Math.abs(this.cannonPitch) < 1e-1f ? 0 : this.cannonPitch;
	}

	public boolean hasLimiter() {
		return !this.cannonLimiter.isEmpty() || this.hasAnyLimit();
	}

	public ItemStack getLimiterStack() {
		return this.cannonLimiter;
	}

	public void installLimiter(ItemStack stack) {
		this.cannonLimiter = stack.copy();
		this.cannonLimiter.setCount(1);
		this.applyLimiterSettings(CannonLimiterSettings.get(this.cannonLimiter));
		this.setChanged();
		this.sendData();
	}

	public ItemStack removeLimiter() {
		if (!this.hasLimiter())
			return ItemStack.EMPTY;
		ItemStack removed = this.cannonLimiter.copy();
		this.cannonLimiter = ItemStack.EMPTY;
		this.clearAllLimits();
		this.setChanged();
		this.sendData();
		return removed;
	}

	private void applyLimiterSettings(CannonLimiterSettings settings) {
		settings.normalize();
		this.hasPitchMin = settings.hasPitchMin;
		this.pitchMin = settings.pitchMin;
		this.hasPitchMax = settings.hasPitchMax;
		this.pitchMax = settings.pitchMax;
		this.hasYawMin = settings.hasYawMin;
		this.yawMin = settings.yawMin;
		this.hasYawMax = settings.hasYawMax;
		this.yawMax = settings.yawMax;
	}

	private void clearAllLimits() {
		this.hasPitchMin = false;
		this.hasPitchMax = false;
		this.hasYawMin = false;
		this.hasYawMax = false;
	}

	private void normalizeLimiterFields() {
		CannonLimiterSettings settings = new CannonLimiterSettings();
		settings.hasPitchMin = this.hasPitchMin;
		settings.pitchMin = this.pitchMin;
		settings.hasPitchMax = this.hasPitchMax;
		settings.pitchMax = this.pitchMax;
		settings.hasYawMin = this.hasYawMin;
		settings.yawMin = this.yawMin;
		settings.hasYawMax = this.hasYawMax;
		settings.yawMax = this.yawMax;
		this.applyLimiterSettings(settings);
	}

	public float setLimit(LimitType type) {
		float value = this.getCurrentLimitValue(type);
		switch (type) {
			case PITCH_MIN -> {
				this.pitchMin = value;
				this.hasPitchMin = true;
			}
			case PITCH_MAX -> {
				this.pitchMax = value;
				this.hasPitchMax = true;
			}
			case YAW_MIN -> {
				this.yawMin = value;
				this.hasYawMin = true;
			}
			case YAW_MAX -> {
				this.yawMax = value;
				this.hasYawMax = true;
			}
		}
		this.setChanged();
		this.sendData();
		return value;
	}

	public void clearLimit(LimitType type) {
		switch (type) {
			case PITCH_MIN -> this.hasPitchMin = false;
			case PITCH_MAX -> this.hasPitchMax = false;
			case YAW_MIN -> this.hasYawMin = false;
			case YAW_MAX -> this.hasYawMax = false;
		}
		this.setChanged();
		this.sendData();
	}

	public float getCurrentLimitValue(LimitType type) {
		return switch (type) {
			case PITCH_MIN, PITCH_MAX -> this.cannonPitch;
			case YAW_MIN, YAW_MAX -> this.getYawDelta();
		};
	}

	public void setLimitedPitch(float pitch) {
		this.cannonPitch = this.clampPitchToLimits(pitch);
		this.applyRotation();
		this.setChanged();
		this.sendData();
	}

	public void setLimitedYaw(float yaw) {
		this.cannonYaw = this.clampYawToLimits(yaw);
		this.applyRotation();
		this.setChanged();
		this.sendData();
	}

	private float clampPitchToLimits(float pitch) {
		float result = pitch;
		if (this.hasPitchMin && this.hasPitchMax) {
			float min = Math.min(this.pitchMin, this.pitchMax);
			float max = Math.max(this.pitchMin, this.pitchMax);
			result = Mth.clamp(result, min, max);
		} else {
			if (this.hasPitchMin)
				result = Math.max(result, this.pitchMin);
			if (this.hasPitchMax)
				result = Math.min(result, this.pitchMax);
		}
		return result;
	}

	private float clampYawToLimits(float yaw) {
		if (!this.hasYawMin && !this.hasYawMax)
			return yaw;
		float neutralYaw = this.getNeutralYaw();
		float delta = Mth.wrapDegrees(yaw - neutralYaw);
		if (this.hasYawMin && this.hasYawMax) {
			float min = Math.min(this.yawMin, this.yawMax);
			float max = Math.max(this.yawMin, this.yawMax);
			delta = Mth.clamp(delta, min, max);
		} else {
			if (this.hasYawMin)
				delta = Math.max(delta, this.yawMin);
			if (this.hasYawMax)
				delta = Math.min(delta, this.yawMax);
		}
		return neutralYaw + delta;
	}

	private float getYawDelta() {
		return Mth.wrapDegrees(this.cannonYaw - this.getNeutralYaw());
	}

	private float getNeutralYaw() {
		if (this.mountedContraption != null)
			return this.getContraptionDirection().toYRot();
		return this.getBlockState().getValue(CompactCannonMountBlock.HORIZONTAL_FACING).toYRot();
	}

	public Direction getContraptionDirection() {
		return this.mountedContraption == null ? Direction.NORTH : ((AbstractMountedCannonContraption) this.mountedContraption.getContraption()).initialOrientation();
	}

	public float getAngularSpeed(float value, float clientDiff) {
		float speed = KineticBlockEntity.convertToAngular(value) * 0.125f;
		if (value == 0)
			speed = 0;
		if (this.getLevel().isClientSide) {
			speed *= ServerSpeedProvider.get();
			speed += clientDiff / 3.0f;
		}
		return speed;
	}

	protected void assemble() throws AssemblyException {
		BlockPos assemblyPos = this.worldPosition.relative(this.getCannonSide());
		if (this.getLevel().isOutsideBuildHeight(assemblyPos))
			throw cannonBlockOutsideOfWorld(assemblyPos);

		AbstractMountedCannonContraption mountedCannon = this.getContraption(assemblyPos);
		if (mountedCannon == null || !mountedCannon.assemble(this.getLevel(), assemblyPos))
			return;
		if (this.getBlockState().getBlock() instanceof CompactCannonMountBlock mountBlock
			&& mountBlock.isAutocannonOnly() && !this.isAutocannonContraption(mountedCannon))
			return;

		Direction facing = this.getBlockState().getValue(CompactCannonMountBlock.HORIZONTAL_FACING);
		Direction cannonFacing = mountedCannon.initialOrientation();
		if (facing.getAxis() != cannonFacing.getAxis() && cannonFacing.getAxis().isHorizontal())
			return;

		this.running = true;
		mountedCannon.removeBlocksFromWorld(this.getLevel(), BlockPos.ZERO);
		PitchOrientedContraptionEntity contraptionEntity = PitchOrientedContraptionEntity.create(this.getLevel(), mountedCannon, cannonFacing, this);
		this.mountedContraption = contraptionEntity;
		this.resetContraptionToOffset();
		this.getLevel().addFreshEntity(contraptionEntity);
		this.refreshItemHandlerCapability();
		this.sendData();
		AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.getLevel(), this.worldPosition);
	}

	private AbstractMountedCannonContraption getContraption(BlockPos pos) {
		return this.level.getBlockState(pos).getBlock() instanceof CannonContraptionProviderBlock provider ? provider.getCannonContraption() : null;
	}

	private boolean isAutocannonContraption(AbstractMountedCannonContraption cannon) {
		return cannon instanceof MountedAutocannonContraption
			|| this.findAutocannonBreech(cannon) != null
			|| (cannon instanceof ItemCannon && this.hasAutocannonTypeName(cannon));
	}

	private boolean hasAutocannonTypeName(AbstractMountedCannonContraption cannon) {
		String className = cannon.getClass().getName().toLowerCase();
		if (className.contains("autocannon"))
			return true;
		try {
			ResourceLocation id = cannon.getCannonType().getId();
			return id != null && id.toString().toLowerCase().contains("autocannon");
		} catch (RuntimeException ignored) {
			return false;
		}
	}

	@Override
	public void disassemble() {
		if (!this.running && this.mountedContraption == null)
			return;
		if (this.mountedContraption != null) {
			this.resetContraptionToOffset();
			this.mountedContraption.save(new CompoundTag());
			this.mountedContraption.disassemble();
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(this.getLevel(), this.worldPosition);
		}
		this.running = false;
		this.refreshItemHandlerCapability();
	}

	protected void resetContraptionToOffset() {
		if (this.mountedContraption == null)
			return;
		this.cannonPitch = 0;
		this.cannonYaw = this.getContraptionDirection().toYRot();
		this.prevPitch = this.cannonPitch;
		this.prevYaw = this.cannonYaw;

		this.mountedContraption.pitch = this.cannonPitch;
		this.mountedContraption.yaw = this.cannonYaw;
		this.mountedContraption.prevPitch = this.mountedContraption.pitch;
		this.mountedContraption.prevYaw = this.mountedContraption.yaw;

		this.mountedContraption.setXRot(this.cannonPitch);
		this.mountedContraption.setYRot(this.cannonYaw);
		this.mountedContraption.xRotO = this.mountedContraption.getXRot();
		this.mountedContraption.yRotO = this.mountedContraption.getYRot();

		Vec3 pos = Vec3.atBottomCenterOf(this.worldPosition.relative(this.getCannonSide()));
		this.mountedContraption.setPos(pos);
	}

	public float calculateCannonStressApplied() {
		if (this.running && this.mountedContraption != null) {
			AbstractMountedCannonContraption contraption = (AbstractMountedCannonContraption) this.mountedContraption.getContraption();
			return contraption.getWeightForStress();
		}
		return 0.0f;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		this.normalizeLimiterFields();
		tag.putBoolean("Running", this.running);
		tag.putFloat("CannonYaw", this.cannonYaw);
		tag.putFloat("CannonPitch", this.cannonPitch);
		AssemblyException.write(tag, this.lastException);

		CompoundTag pitchTag = new CompoundTag();
		this.pitchInterface.saveAdditional(pitchTag);
		tag.put("PitchInterface", pitchTag);

		CompoundTag yawTag = new CompoundTag();
		this.yawInterface.saveAdditional(yawTag);
		tag.put("YawInterface", yawTag);

		if (this.reassemble)
			tag.putBoolean("TryReassembling", true);
		if (this.hasPitchMin)
			tag.putFloat("PitchMin", this.pitchMin);
		if (this.hasPitchMax)
			tag.putFloat("PitchMax", this.pitchMax);
		if (this.hasYawMin)
			tag.putFloat("YawMin", this.yawMin);
		if (this.hasYawMax)
			tag.putFloat("YawMax", this.yawMax);
		if (!this.cannonLimiter.isEmpty())
			tag.put("CannonLimiter", this.cannonLimiter.save(new CompoundTag()));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		boolean oldRunning = this.running;
		this.running = tag.getBoolean("Running");
		this.cannonYaw = tag.getFloat("CannonYaw");
		this.cannonPitch = tag.getFloat("CannonPitch");
		this.lastException = AssemblyException.read(tag);

		if (clientPacket) {
			this.pitchInterface.readClient(tag.getCompound("PitchInterface"));
			this.yawInterface.readClient(tag.getCompound("YawInterface"));
		} else {
			this.pitchInterface.load(tag.getCompound("PitchInterface"));
			this.yawInterface.load(tag.getCompound("YawInterface"));
		}
		this.reassemble = tag.contains("TryReassembling");
		this.hasPitchMin = tag.contains("PitchMin");
		this.pitchMin = this.hasPitchMin ? tag.getFloat("PitchMin") : 0;
		this.hasPitchMax = tag.contains("PitchMax");
		this.pitchMax = this.hasPitchMax ? tag.getFloat("PitchMax") : 0;
		this.hasYawMin = tag.contains("YawMin");
		this.yawMin = this.hasYawMin ? tag.getFloat("YawMin") : 0;
		this.hasYawMax = tag.contains("YawMax");
		this.yawMax = this.hasYawMax ? tag.getFloat("YawMax") : 0;
		this.cannonLimiter = tag.contains("CannonLimiter") ? ItemStack.of(tag.getCompound("CannonLimiter")) : ItemStack.EMPTY;
		if (!this.cannonLimiter.isEmpty())
			this.applyLimiterSettings(CannonLimiterSettings.get(this.cannonLimiter));
		this.normalizeLimiterFields();

		if (!clientPacket)
			return;

		if (this.running) {
			if (oldRunning && (this.mountedContraption == null || !this.mountedContraption.isStalled())) {
				this.clientYawDiff = AngleHelper.getShortestAngleDiff(this.prevYaw, this.cannonYaw);
				this.clientPitchDiff = AngleHelper.getShortestAngleDiff(this.prevPitch, this.cannonPitch);
				this.prevYaw = this.cannonYaw;
				this.prevPitch = this.cannonPitch;
			}
		} else {
			this.mountedContraption = null;
		}
	}

	@Override
	public void remove() {
		this.remove = true;
		if (!this.getLevel().isClientSide)
			this.disassemble();
		this.pitchInterface.remove();
		this.yawInterface.remove();
		super.remove();
	}

	@Override public boolean isAttachedTo(AbstractContraptionEntity entity) { return this.mountedContraption == entity; }

	@Override
	public void attach(PitchOrientedContraptionEntity contraption) {
		if (!(contraption.getContraption() instanceof AbstractMountedCannonContraption))
			return;
		this.mountedContraption = contraption;
		if (!this.getLevel().isClientSide) {
			this.running = true;
			this.refreshItemHandlerCapability();
			this.sendData();
		}
	}

	@Override
	public void onStall() {
		if (!this.getLevel().isClientSide)
			this.sendData();
	}

	@Override public BlockPos getControllerBlockPos() { return this.worldPosition; }

	public void markForReassembly() {
		this.reassemble = true;
	}

	@Override
	public BlockPos getDismountPositionForContraption(PitchOrientedContraptionEntity poce) {
		return this.worldPosition.relative(this.getCannonSide().getOpposite());
	}

	@Override public AssemblyException getLastAssemblyException() { return this.lastException; }

	public Vec3 getInteractionLocation() {
		return this.mountedContraption != null && this.mountedContraption.getContraption() instanceof AbstractMountedCannonContraption cannon
			? cannon.getInteractionVec(this.mountedContraption) : Vec3.atCenterOf(this.worldPosition);
	}

	@Nullable public PitchOrientedContraptionEntity getContraption() { return this.mountedContraption; }

	private Direction getCannonSide() {
		BlockState state = this.getBlockState();
		return ((CompactCannonMountBlock) state.getBlock()).getCannonSide(state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return this.itemHandler.cast();
		return super.getCapability(cap, side);
	}

	private void refreshItemHandlerCapability() {
		this.itemHandler.invalidate();
		this.itemHandler = LazyOptional.of(() -> new MountItemHandler());
	}

	@Nullable
	private IItemHandler getMountedItemHandler() {
		return this.mountedContraption == null
			? null
			: this.mountedContraption.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
	}

	@Nullable
	private ItemCannon getMountedItemCannon() {
		return this.mountedContraption != null && this.mountedContraption.getContraption() instanceof ItemCannon cannon ? cannon : null;
	}

	@Nullable
	private AbstractAutocannonBreechBlockEntity findMountedAutocannonBreech() {
		if (this.mountedContraption == null
			|| !(this.mountedContraption.getContraption() instanceof AbstractMountedCannonContraption cannon))
			return null;
		return this.findAutocannonBreech(cannon);
	}

	@Nullable
	private AbstractAutocannonBreechBlockEntity findAutocannonBreech(AbstractMountedCannonContraption cannon) {
		BlockEntity startBlockEntity = cannon.presentBlockEntities.get(cannon.getStartPos());
		if (startBlockEntity instanceof AbstractAutocannonBreechBlockEntity breech)
			return breech;
		for (BlockEntity blockEntity : cannon.presentBlockEntities.values())
			if (blockEntity instanceof AbstractAutocannonBreechBlockEntity breech)
				return breech;
		return null;
	}

	private ItemStack insertAutocannonAmmoContainer(ItemStack stack, boolean simulate) {
		if (!(stack.getItem() instanceof AutocannonAmmoContainerItem))
			return stack;
		AbstractAutocannonBreechBlockEntity breech = this.findMountedAutocannonBreech();
		if (breech == null)
			return stack;
		ItemStack oldContainer = breech.getMagazine();
		if (oldContainer.getItem() instanceof AutocannonAmmoContainerItem
			&& AutocannonAmmoContainerItem.getTotalAmmoCount(oldContainer) > 0)
			return stack;
		if (simulate)
			return ItemStack.EMPTY;
		ItemStack inserted = stack.copy();
		inserted.setCount(1);
		breech.setMagazine(inserted);
		breech.setChanged();
		return oldContainer.isEmpty() ? ItemStack.EMPTY : oldContainer.copy();
	}

	private ItemStack insertLooseAutocannonAmmo(ItemStack stack, boolean simulate) {
		if (!(stack.getItem() instanceof AutocannonAmmoItem))
			return stack;
		AbstractAutocannonBreechBlockEntity breech = this.findMountedAutocannonBreech();
		if (breech == null || breech.isInputFull())
			return stack;
		ItemStack remainder = stack.copy();
		remainder.shrink(1);
		if (!simulate) {
			ItemStack inserted = stack.copy();
			inserted.setCount(1);
			breech.getInputBuffer().add(inserted);
			breech.setChanged();
		}
		return remainder;
	}

	public ItemStack insertAutocannonFeedAmmo(ItemStack stack, boolean simulate) {
		ItemStack insertedAmmo = this.insertLooseAutocannonAmmo(stack, simulate);
		if (insertedAmmo.getCount() != stack.getCount() || !ItemStack.matches(insertedAmmo, stack))
			return insertedAmmo;
		ItemCannon itemCannon = this.getMountedItemCannon();
		return itemCannon == null ? stack : itemCannon.insertItemIntoCannon(stack, simulate);
	}

	public ItemStack insertCannonMagazineAmmo(ItemStack stack, boolean simulate) {
		if (this.mountedContraption == null
			|| !(this.mountedContraption.getContraption() instanceof MountedBigCannonContraption cannon))
			return stack;
		return CannonMountPoint.bigCannonInsert(stack, simulate, cannon, this.mountedContraption);
	}

	private class MountItemHandler implements IItemHandler {
		@Override
		public int getSlots() {
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null ? 1 : mountedHandler.getSlots();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null ? ItemStack.EMPTY : mountedHandler.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack insertedContainer = CompactCannonMountBlockEntity.this.insertAutocannonAmmoContainer(stack, simulate);
			if (insertedContainer.getCount() != stack.getCount() || !ItemStack.matches(insertedContainer, stack))
				return insertedContainer;
			ItemStack insertedAmmo = CompactCannonMountBlockEntity.this.insertLooseAutocannonAmmo(stack, simulate);
			if (insertedAmmo.getCount() != stack.getCount() || !ItemStack.matches(insertedAmmo, stack))
				return insertedAmmo;
			ItemCannon itemCannon = CompactCannonMountBlockEntity.this.getMountedItemCannon();
			if (itemCannon != null)
				return itemCannon.insertItemIntoCannon(stack, simulate);
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null ? stack : mountedHandler.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemCannon itemCannon = CompactCannonMountBlockEntity.this.getMountedItemCannon();
			if (itemCannon != null)
				return itemCannon.extractItemFromCannon(simulate);
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null ? ItemStack.EMPTY : mountedHandler.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null ? 64 : mountedHandler.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			IItemHandler mountedHandler = CompactCannonMountBlockEntity.this.getMountedItemHandler();
			return mountedHandler == null || mountedHandler.isItemValid(slot, stack);
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (IRotate.StressImpact.isEnabled()) {
			float pitchStress = this.pitchInterface.calculateStressApplied();
			float yawStress = this.yawInterface.calculateStressApplied();
			if (!Mth.equal(pitchStress + yawStress, 0)) {
				Lang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
				Lang.translate("tooltip.stressImpact").style(GRAY).forGoggles(tooltip);
				float stressTotal = pitchStress * Math.abs(this.pitchInterface.getTheoreticalSpeed())
					+ yawStress * Math.abs(this.yawInterface.getTheoreticalSpeed());
				Lang.number(stressTotal)
					.translate("generic.unit.stress")
					.style(ChatFormatting.AQUA)
					.space()
					.add(Lang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
					.forGoggles(tooltip, 1);
			}
		}
		ExtendsCannonMount.addCannonInfoToTooltip(tooltip, this.mountedContraption);
		this.addLimiterTooltip(tooltip);
		return true;
	}

	private void addLimiterTooltip(List<Component> tooltip) {
		if (!this.hasAnyLimit()) {
			tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.none")
				.withStyle(ChatFormatting.DARK_GRAY));
			return;
		}
		tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.header")
			.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.pitch_min",
				formatLimit(this.hasPitchMin, this.pitchMin))
			.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.pitch_max",
				formatLimit(this.hasPitchMax, this.pitchMax))
			.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.yaw_min",
				formatLimit(this.hasYawMin, this.yawMin))
			.withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.translatable("block.cbc_firepower_components.compact_cannon_mount.limiter.yaw_max",
				formatLimit(this.hasYawMax, this.yawMax))
			.withStyle(ChatFormatting.YELLOW));
	}

	private boolean hasAnyLimit() {
		return this.hasPitchMin || this.hasPitchMax || this.hasYawMin || this.hasYawMax;
	}

	private static String formatLimit(boolean hasLimit, float value) {
		return hasLimit ? String.format(Locale.ROOT, "%.1f", value) : "-";
	}

	@Nullable
	@Override
	public KineticBlockEntity getInterfacingBlockEntity(BlockPos from) {
		if (from.getX() == 0 && from.getZ() == 0 && Math.abs(from.getY()) == 1)
			return this.yawInterface;
		BlockState state = this.getBlockState();
		CompactCannonMountBlock block = (CompactCannonMountBlock) state.getBlock();
		Direction.Axis axis = block.getRotationAxis(state);
		BlockPos pitchInput = BlockPos.ZERO.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
		return from.equals(pitchInput) || from.equals(BlockPos.ZERO.subtract(pitchInput)) ? this.pitchInterface : null;
	}

	@Override
	public List<KineticBlockEntity> getAllKineticBlockEntities() {
		return List.of(this.pitchInterface, this.yawInterface);
	}

	public void tryUpdatingSpeed() {
		this.pitchInterface.tryUpdateSpeed();
		this.yawInterface.tryUpdateSpeed();
	}

	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		this.pitchInterface.setBlockState(blockState);
		this.yawInterface.setBlockState(blockState);
	}

}
