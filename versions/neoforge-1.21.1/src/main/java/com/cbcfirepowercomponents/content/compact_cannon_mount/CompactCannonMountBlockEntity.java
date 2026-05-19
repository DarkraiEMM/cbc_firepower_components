package com.cbcfirepowercomponents.content.compact_cannon_mount;

import static net.minecraft.ChatFormatting.GRAY;
import static rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity.cannonBlockOutsideOfWorld;

import java.util.List;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
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

	private AssemblyException lastException = null;
	protected PitchOrientedContraptionEntity mountedContraption;
	private boolean running;

	private float cannonYaw;
	private float cannonPitch;
	private float prevYaw;
	private float prevPitch;
	private float clientYawDiff;
	private float clientPitchDiff;

	protected final CompactCannonMountInterfaceBlockEntity pitchInterface;
	protected final CompactCannonMountInterfaceBlockEntity yawInterface;
	private final IItemHandler itemHandler = new MountItemHandler();

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
		return ResourceLocation.fromNamespaceAndPath(FirepowerComponents.MOD_ID, "compact_cannon_mount");
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(this.getBlockPos()).expandTowards(Vec3.atLowerCornerOf(this.getCannonSide().getNormal()));
	}

	@Override
	public void tick() {
		super.tick();
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

			this.cannonYaw = (this.cannonYaw + yawSpeed) % 360.0f;
			float newPitch = this.cannonPitch + pitchSpeed * sign;
			this.cannonPitch = Mth.clamp(newPitch % 360.0f, -this.getMaxDepress(), this.getMaxElevate());
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
			this.cannonPitch = Mth.clamp(this.mountedContraption.pitch, minPitch, maxPitch) * sign;
			this.cannonYaw = this.mountedContraption.yaw;
		} else {
			this.mountedContraption.pitch = this.cannonPitch * sign;
			this.mountedContraption.yaw = this.cannonYaw;
		}
	}

	public void onRedstoneUpdate(boolean assemblyPowered, boolean prevAssemblyPowered, boolean firePowered, boolean prevFirePowered, int firePower) {
		if (assemblyPowered != prevAssemblyPowered) {
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
		float pitchOffset = Mth.lerp(partialTicks, this.cannonPitch, this.cannonPitch + speed * horizontalSign);
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
		return Mth.lerp(partialTicks, this.cannonYaw, this.cannonYaw + speed);
	}

	public float getDisplayPitch() {
		return Math.abs(this.cannonPitch) < 1e-1f ? 0 : this.cannonPitch;
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
		this.invalidateCapabilities();
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
		this.invalidateCapabilities();
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
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putBoolean("Running", this.running);
		tag.putFloat("CannonYaw", this.cannonYaw);
		tag.putFloat("CannonPitch", this.cannonPitch);
		AssemblyException.write(tag, registries, this.lastException);

		CompoundTag pitchTag = new CompoundTag();
		this.pitchInterface.saveAdditional(pitchTag, registries);
		tag.put("PitchInterface", pitchTag);

		CompoundTag yawTag = new CompoundTag();
		this.yawInterface.saveAdditional(yawTag, registries);
		tag.put("YawInterface", yawTag);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		boolean oldRunning = this.running;
		this.running = tag.getBoolean("Running");
		this.cannonYaw = tag.getFloat("CannonYaw");
		this.cannonPitch = tag.getFloat("CannonPitch");
		this.lastException = AssemblyException.read(tag, registries);

		if (clientPacket) {
			this.pitchInterface.readClient(tag.getCompound("PitchInterface"), registries);
			this.yawInterface.readClient(tag.getCompound("YawInterface"), registries);
		} else {
			this.pitchInterface.loadWithComponents(tag.getCompound("PitchInterface"), registries);
			this.yawInterface.loadWithComponents(tag.getCompound("YawInterface"), registries);
		}

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
			this.invalidateCapabilities();
			this.sendData();
		}
	}

	@Override
	public void onStall() {
		if (!this.getLevel().isClientSide)
			this.sendData();
	}

	@Override public BlockPos getControllerBlockPos() { return this.worldPosition; }

	@Override
	public Vec3 getDismountPositionForContraption(PitchOrientedContraptionEntity poce) {
		return Vec3.atBottomCenterOf(this.worldPosition.relative(this.getCannonSide().getOpposite()));
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

	@Nullable
	public IItemHandler getItemHandler(Direction side) {
		return this.itemHandler;
	}

	@Nullable
	private IItemHandler getMountedItemHandler() {
		return this.mountedContraption == null ? null : this.mountedContraption.getCapability(Capabilities.ItemHandler.ENTITY);
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
		if (!sameStackAndCount(insertedAmmo, stack))
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

	private static boolean sameStackAndCount(ItemStack first, ItemStack second) {
		return ItemStack.matches(first, second) && first.getCount() == second.getCount();
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
				CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
				CreateLang.translate("tooltip.stressImpact").style(GRAY).forGoggles(tooltip);
				float stressTotal = pitchStress * Math.abs(this.pitchInterface.getTheoreticalSpeed())
					+ yawStress * Math.abs(this.yawInterface.getTheoreticalSpeed());
				CreateLang.number(stressTotal)
					.translate("generic.unit.stress")
					.style(ChatFormatting.AQUA)
					.space()
					.add(CreateLang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
					.forGoggles(tooltip, 1);
			}
		}
		ExtendsCannonMount.addCannonInfoToTooltip(tooltip, this.mountedContraption);
		return true;
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
