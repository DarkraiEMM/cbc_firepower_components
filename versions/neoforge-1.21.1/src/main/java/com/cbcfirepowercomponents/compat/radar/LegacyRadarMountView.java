package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.index.CBCBlockEntities;
import rbasamoyai.createbigcannons.index.CBCBlocks;

/** Read-through CBC view for Radar 4.9, which predates public mount adapters.
 * Never registered in the level or ticked as a separate block entity. */
final class LegacyRadarMountView extends CannonMountBlockEntity {
	final CompactCannonMountBlockEntity owner;
	LegacyRadarMountView(CompactCannonMountBlockEntity owner) {
		super(CBCBlockEntities.CANNON_MOUNT.get(), owner.getBlockPos(), CBCBlocks.CANNON_MOUNT.getDefaultState());
		this.owner = owner;
		setLevel(owner.getLevel());
	}
	@Override public boolean isRemoved() {
		return owner == null || owner.isRemoved() || owner.getLevel() == null
			|| !owner.getLevel().hasChunkAt(owner.getBlockPos()) || owner.getLevel().getBlockEntity(owner.getBlockPos()) != owner;
	}
	@Override public PitchOrientedContraptionEntity getContraption() { return isRemoved() ? null : owner.getContraption(); }
	@Override public boolean isRunning() { return !isRemoved() && owner.isRunning(); }
	@Override public Vec3 getInteractionLocation() { return owner.getInteractionLocation(); }
	@Override public float getDisplayPitch() { return owner.getCannonPitch(); }
	@Override public float getPitchOffset(float partial) { return owner.getCannonPitch(); }
	@Override public float getYawOffset(float partial) { return owner.getCannonYaw(); }
	@Override public void setPitch(float pitch) { owner.setLimitedPitch(pitch); }
	@Override public void setYaw(float yaw) { owner.setLimitedYaw(yaw); }
}
