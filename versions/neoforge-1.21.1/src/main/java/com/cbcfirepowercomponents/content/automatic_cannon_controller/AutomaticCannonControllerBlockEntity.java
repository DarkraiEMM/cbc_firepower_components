package com.cbcfirepowercomponents.content.automatic_cannon_controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.cbcfirepowercomponents.compat.DriveByWireCompat;
import com.cbcfirepowercomponents.content.AmmunitionSelectionSource;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackStructuralBlock;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.OpenControllerConfigPacket;
import com.cbcfirepowercomponents.network.OpenControllerAmmunitionPacket;
import com.cbcfirepowercomponents.content.CannonAmmunitionHelper;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AutomaticCannonControllerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
	public enum FireMode {
		SINGLE("single"), BURST("burst"), CONTINUOUS("continuous");
		private final String translation;
		FireMode(String translation) { this.translation = translation; }
	}

	public enum CoordinationMode {
		POLLING("polling"), SALVO("salvo");
		private final String translation;
		CoordinationMode(String translation) { this.translation = translation; }
	}

	private static final int INTERACTION_REPEAT_GAP = 6;

	private FireMode mode = FireMode.SINGLE;
	private CoordinationMode coordinationMode = CoordinationMode.POLLING;
	private int outputSignalStrength = 15;
	private boolean commandPowered;
	private boolean lastCommandPowered;
	private boolean outputPowered;
	private boolean continuousLatched;
	private boolean manualFireQueued;
	private int shotsRemaining;
	private int sequenceTick;
	private int burstShotInterval = 6;
	private int pollingIndex;
	private final List<BlockPos> activeMounts = new ArrayList<>();
	@Nullable private UUID interactingPlayer;
	private long interactionStartTick;
	private long lastInteractionTick;
	private boolean holdHandled;

	public AutomaticCannonControllerBlockEntity(BlockPos pos, BlockState state) {
		super(MTBlockEntities.AUTOMATIC_CANNON_CONTROLLER.get(), pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	public void refreshRedstoneCommands() {
		if (this.level == null)
			return;
		boolean previousCommand = this.commandPowered;
		Direction facing = this.getBlockState().getValue(AutomaticCannonControllerBlock.FACING);
		Direction back = facing.getAxis().isHorizontal() ? facing.getOpposite() : Direction.SOUTH;
		this.commandPowered = this.level.getSignal(this.worldPosition.relative(back), back) > 0;
		if (previousCommand != this.commandPowered) {
			this.setChanged();
			this.sendData();
		}
	}

	@Nullable
	public Component handlePrimaryInteraction(Player player) {
		if (this.level == null)
			return null;
		long now = this.level.getGameTime();
		UUID playerId = player.getUUID();
		if (!playerId.equals(this.interactingPlayer) || now - this.lastInteractionTick > INTERACTION_REPEAT_GAP) {
			this.interactingPlayer = playerId;
			this.interactionStartTick = now;
			this.lastInteractionTick = now;
			this.holdHandled = false;
			return null;
		}
		this.lastInteractionTick = now;
		return null;
	}

	public void openConfiguration(Player player) {
		if (this.level == null || this.level.isClientSide)
			return;
		this.holdHandled = true;
		this.clearInteraction();
		MTNetwork.sendToPlayer(player, new OpenControllerConfigPacket(
			this.worldPosition, this.mode.ordinal(), this.coordinationMode.ordinal(), this.outputSignalStrength));
	}

	public void cancelPendingInteraction(Player player) {
		if (player.getUUID().equals(this.interactingPlayer))
			this.clearInteraction();
	}

	private void finishPendingInteraction() {
		if (this.level == null || this.interactingPlayer == null)
			return;
		if (this.level.getGameTime() - this.lastInteractionTick <= INTERACTION_REPEAT_GAP)
			return;
		if (!this.holdHandled)
			this.manualFireQueued = true;
		this.clearInteraction();
	}

	private void clearInteraction() {
		this.interactingPlayer = null;
		this.holdHandled = false;
	}

	public void setConfiguration(int fireMode, int coordination, int signalStrength) {
		if (fireMode < 0 || fireMode >= FireMode.values().length
			|| coordination < 0 || coordination >= CoordinationMode.values().length
			|| signalStrength < 1 || signalStrength > 15)
			return;
		this.mode = FireMode.values()[fireMode];
		this.coordinationMode = CoordinationMode.values()[coordination];
		this.outputSignalStrength = signalStrength;
		this.shotsRemaining = 0;
		this.continuousLatched = false;
		this.setOutput(false);
		this.setChanged();
		this.sendData();
	}

	public Component getModeMessage() {
		return Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.mode." + this.mode.translation);
	}

	public Component getCoordinationModeMessage() {
		return Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.coordination."
			+ this.coordinationMode.translation);
	}

	public Component getStatusMessage() {
		return Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.status",
			this.getModeMessage(), this.getCoordinationModeMessage(), this.countControlledMounts(), this.countControlledCompartments(),
			Component.translatable(this.commandPowered ? "gui.cbc_firepower_components.on" : "gui.cbc_firepower_components.off"));
	}

	public boolean selectNextAmmunition() {
		if (this.level == null)
			return false;
		List<AmmunitionSelectionSource> compartments = this.getControlledAmmunitionSources();
		for (AmmunitionSelectionSource compartment : compartments)
			compartment.selectNextType();
		return !compartments.isEmpty();
	}

	public void openAmmunitionSelection(Player player) {
		List<AmmunitionSelectionSource> compartments = this.getControlledAmmunitionSources();
		if (compartments.isEmpty()) {
			player.displayClientMessage(Component.translatable(
				"block.cbc_firepower_components.automatic_cannon_controller.no_ammunition_compartment"), true);
			return;
		}
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> combined = new ArrayList<>();
		for (AmmunitionSelectionSource compartment : compartments) {
			for (ReadyAmmunitionCompartmentBlockEntity.RoundType candidate : compartment.getAvailableRoundTypes()) {
				int existing = -1;
				for (int i = 0; i < combined.size(); ++i) {
					ReadyAmmunitionCompartmentBlockEntity.RoundType option = combined.get(i);
					if (CannonAmmunitionHelper.sameRound(option.projectile(), option.propellant(),
						candidate.projectile(), candidate.propellant())) {
						existing = i;
						break;
					}
				}
				if (existing < 0) {
					combined.add(candidate);
				} else {
					ReadyAmmunitionCompartmentBlockEntity.RoundType option = combined.get(existing);
					combined.set(existing, new ReadyAmmunitionCompartmentBlockEntity.RoundType(
						option.projectile(), option.propellant(), option.count() + candidate.count(),
						option.selected() || candidate.selected()));
				}
			}
		}
		if (combined.isEmpty()) {
			player.displayClientMessage(Component.translatable(
				"block.cbc_firepower_components.ready_ammunition_compartment.empty"), true);
			return;
		}
		MTNetwork.sendToPlayer(player, new OpenControllerAmmunitionPacket(this.worldPosition, combined));
	}

	public boolean selectAmmunition(ItemStack projectile, ItemStack propellant) {
		boolean changed = false;
		for (AmmunitionSelectionSource compartment : this.getControlledAmmunitionSources())
			changed |= compartment.selectType(projectile, propellant);
		return changed;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AutomaticCannonControllerBlockEntity controller) {
		controller.tick();
	}

	@Override
	public void tick() {
		super.tick();
		this.finishPendingInteraction();
		this.tickController();
	}

	private void tickController() {
		this.refreshRedstoneCommands();
		boolean rising = this.commandPowered && !this.lastCommandPowered;
		this.lastCommandPowered = this.commandPowered;
		if (rising || this.manualFireQueued) {
			this.manualFireQueued = false;
			this.acceptFireCommand();
		}
		if (this.mode == FireMode.CONTINUOUS)
			this.runContinuousSequence();
		else
			this.runPulseSequence();
	}

	private void acceptFireCommand() {
		switch (this.mode) {
			case SINGLE -> {
				this.shotsRemaining = 1;
				this.sequenceTick = 0;
			}
			case BURST -> {
				this.shotsRemaining = 3;
				this.sequenceTick = 0;
			}
			case CONTINUOUS -> {
				this.continuousLatched = !this.continuousLatched;
				this.sequenceTick = 0;
				if (!this.continuousLatched)
					this.setOutput(false);
			}
		}
	}

	private void runContinuousSequence() {
		if (!this.continuousLatched) {
			this.setOutput(false);
			return;
		}
		if (this.coordinationMode == CoordinationMode.SALVO) {
			// In salvo mode every mount stays powered and follows its own native
			// firing cycle at the selected signal strength.
			this.setOutput(true);
			return;
		}

		// Polling advances one mount per actual firing interval. Keeping a
		// single mount powered here would stop the polling index after its first
		// selection; using a fixed interval would override the cannon's rate.
		if (this.outputPowered && this.sequenceTick >= this.burstShotInterval) {
			this.setOutput(false);
			this.sequenceTick = 0;
		}
		if (!this.outputPowered) {
			this.setOutput(true);
			this.burstShotInterval = this.resolveBurstShotInterval();
		}
		++this.sequenceTick;
	}

	private void runPulseSequence() {
		if (this.shotsRemaining <= 0) {
			this.setOutput(false);
			return;
		}
		if (this.mode == FireMode.SINGLE) {
			this.setOutput(true);
			this.shotsRemaining = 0;
			return;
		}
		if (this.outputPowered && this.sequenceTick >= this.burstShotInterval) {
			this.setOutput(false);
			this.sequenceTick = 0;
		}
		if (!this.outputPowered) {
			this.setOutput(true);
			this.burstShotInterval = this.resolveBurstShotInterval();
			--this.shotsRemaining;
		}
		++this.sequenceTick;
	}

	private int resolveBurstShotInterval() {
		int interval = 0;
		if (this.level != null) {
			for (BlockPos pos : this.activeMounts) {
				if (this.level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)
					interval = Math.max(interval, mount.getAutomaticFireIntervalTicks());
			}
		}
		if (interval > 0)
			return Math.max(1, interval);
		return fallbackAutocannonInterval(this.outputSignalStrength);
	}

	private static int fallbackAutocannonInterval(int signalStrength) {
		int[] intervals = {120, 80, 60, 48, 40, 30, 24, 20, 15, 12, 10, 8, 6, 5, 4};
		return intervals[Math.max(1, Math.min(15, signalStrength)) - 1];
	}

	private void setOutput(boolean powered) {
		if (this.outputPowered == powered)
			return;
		this.outputPowered = powered;
		if (!powered) {
			if (this.level != null) {
				for (BlockPos pos : this.activeMounts)
					if (this.level.getBlockEntity(pos) instanceof CompactCannonMountBlockEntity mount)
						mount.setAutomaticFirePowered(false, 0);
			}
			this.activeMounts.clear();
			return;
		}
		List<CompactCannonMountBlockEntity> mounts = this.getControlledMounts();
		if (mounts.isEmpty())
			return;
		if (this.coordinationMode == CoordinationMode.SALVO) {
			for (CompactCannonMountBlockEntity mount : mounts) {
				mount.setAutomaticFirePowered(true, this.outputSignalStrength);
				this.activeMounts.add(mount.getBlockPos());
			}
		} else {
			CompactCannonMountBlockEntity mount = mounts.get(Math.floorMod(this.pollingIndex++, mounts.size()));
			mount.setAutomaticFirePowered(true, this.outputSignalStrength);
			this.activeMounts.add(mount.getBlockPos());
			this.setChanged();
		}
	}

	private List<CompactCannonMountBlockEntity> getControlledMounts() {
		List<CompactCannonMountBlockEntity> mounts = new ArrayList<>();
		if (this.level == null)
			return mounts;
		List<BlockPos> linkedTargets = DriveByWireCompat.getLinkedTargets(this.level, this.worldPosition);
		for (BlockPos targetPos : linkedTargets)
			if (this.level.getBlockEntity(targetPos) instanceof CompactCannonMountBlockEntity mount)
				mounts.add(mount);
		for (Direction direction : Direction.values()) {
			if (this.level.getBlockEntity(this.worldPosition.relative(direction))
				instanceof CompactCannonMountBlockEntity mount
				&& mounts.stream().noneMatch(existing -> existing.getBlockPos().equals(mount.getBlockPos())))
				mounts.add(mount);
		}
		return mounts;
	}

	private int countControlledMounts() {
		if (this.level == null)
			return 0;
		return this.getControlledMounts().size();
	}

	private int countControlledCompartments() {
		if (this.level == null)
			return 0;
		return this.getControlledAmmunitionSources().size();
	}

	private List<AmmunitionSelectionSource> getControlledAmmunitionSources() {
		List<AmmunitionSelectionSource> compartments = new ArrayList<>();
		if (this.level == null)
			return compartments;
		List<BlockPos> linkedTargets = DriveByWireCompat.getLinkedTargets(this.level, this.worldPosition);
		for (BlockPos targetPos : linkedTargets)
			this.addAmmunitionSource(compartments, targetPos);
		for (Direction direction : Direction.values())
			this.addAmmunitionSource(compartments, this.worldPosition.relative(direction));
		return compartments;
	}

	private void addAmmunitionSource(List<AmmunitionSelectionSource> sources, BlockPos targetPos) {
		BlockEntity target = this.level.getBlockEntity(targetPos);
		if (!(target instanceof AmmunitionSelectionSource)) {
			BlockState targetState = this.level.getBlockState(targetPos);
			if (targetState.getBlock() instanceof CarouselAmmunitionRackStructuralBlock)
				target = this.level.getBlockEntity(CarouselAmmunitionRackStructuralBlock.corePos(targetPos, targetState));
		}
		if (target instanceof AmmunitionSelectionSource source
			&& sources.stream().noneMatch(existing -> existing.getBlockPos().equals(source.getBlockPos())))
			sources.add(source);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(this.getModeMessage());
		tooltip.add(this.getCoordinationModeMessage());
		tooltip.add(Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.status.targets",
			this.countControlledMounts(), this.countControlledCompartments()));
		tooltip.add(Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.status.input",
			Component.translatable(this.commandPowered ? "gui.cbc_firepower_components.on" : "gui.cbc_firepower_components.off")));
		if (isPlayerSneaking)
			tooltip.add(Component.translatable("block.cbc_firepower_components.automatic_cannon_controller.input_help"));
		return true;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("Mode", this.mode.ordinal());
		tag.putInt("CoordinationMode", this.coordinationMode.ordinal());
		tag.putInt("OutputSignalStrength", this.outputSignalStrength);
		tag.putInt("PollingIndex", this.pollingIndex);
		tag.putBoolean("CommandPowered", this.commandPowered);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		int modeIndex = tag.getInt("Mode");
		this.mode = modeIndex >= 0 && modeIndex < FireMode.values().length ? FireMode.values()[modeIndex] : FireMode.SINGLE;
		int coordinationIndex = tag.getInt("CoordinationMode");
		this.coordinationMode = coordinationIndex >= 0 && coordinationIndex < CoordinationMode.values().length
			? CoordinationMode.values()[coordinationIndex] : CoordinationMode.POLLING;
		int signalStrength = tag.contains("OutputSignalStrength") ? tag.getInt("OutputSignalStrength") : 15;
		this.outputSignalStrength = Math.max(1, Math.min(15, signalStrength));
		this.pollingIndex = Math.max(0, tag.getInt("PollingIndex"));
		this.commandPowered = tag.getBoolean("CommandPowered");
		this.lastCommandPowered = this.commandPowered;
		this.outputPowered = false;
		this.continuousLatched = false;
		this.manualFireQueued = false;
		this.activeMounts.clear();
		this.shotsRemaining = 0;
	}
}
