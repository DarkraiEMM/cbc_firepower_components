package com.cbcfirepowercomponents.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SwapCarouselRackSlotsPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CarouselAmmunitionRackScreen extends AbstractSimiScreen {
	private static final int SLOT_SIZE = 22;
	private static final int RADIUS = 92;
	private final BlockPos rackPos;
	private int currentIndex;
	private int targetIndex;
	private final List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots;
	private long lastSyncedGameTime = Long.MIN_VALUE;
	private int selectedSlot = -1;
	private int centerX;
	private int centerY;

	public CarouselAmmunitionRackScreen(BlockPos rackPos, int currentIndex, int targetIndex,
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) {
		super(Component.translatable("screen.cbc_firepower_components.carousel_ammunition_rack.title"));
		this.rackPos = rackPos;
		this.currentIndex = currentIndex;
		this.targetIndex = targetIndex;
		this.slots = new ArrayList<>(slots);
		while (this.slots.size() < CarouselAmmunitionRackBlockEntity.CAPACITY)
			this.slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(ItemStack.EMPTY, ItemStack.EMPTY));
	}

	@Override protected void init() {
		this.setWindowSize(260, 270);
		super.init();
		this.centerX = this.guiLeft + this.windowWidth / 2;
		this.centerY = this.guiTop + 139;
		IconButton confirm = new IconButton(this.guiLeft + 232, this.guiTop + 244, AllIcons.I_CONFIRM);
		confirm.withCallback(this::onClose);
		confirm.setToolTip(Component.translatable("gui.done"));
		this.addRenderableWidget(confirm);
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int slot = this.slotAt(mouseX, mouseY);
			if (slot >= 0) {
				if (this.selectedSlot < 0) this.selectedSlot = slot;
				else if (this.selectedSlot == slot) this.selectedSlot = -1;
				else {
					Collections.swap(this.slots, this.selectedSlot, slot);
					MTNetwork.sendToServer(new SwapCarouselRackSlotsPacket(this.rackPos, this.selectedSlot, slot));
					this.selectedSlot = -1;
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private int slotAt(double mouseX, double mouseY) {
		for (int i = 0; i < CarouselAmmunitionRackBlockEntity.CAPACITY; ++i) {
			int x = this.slotX(i), y = this.slotY(i);
			if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) return i;
		}
		return -1;
	}

	private int slotX(int slot) {
		int visualSlot = Math.floorMod(slot - this.currentIndex, CarouselAmmunitionRackBlockEntity.CAPACITY);
		double angle = -Math.PI / 2 + Math.PI * 2 * visualSlot / CarouselAmmunitionRackBlockEntity.CAPACITY;
		return this.centerX + (int) Math.round(Math.cos(angle) * RADIUS) - SLOT_SIZE / 2;
	}

	private int slotY(int slot) {
		int visualSlot = Math.floorMod(slot - this.currentIndex, CarouselAmmunitionRackBlockEntity.CAPACITY);
		double angle = -Math.PI / 2 + Math.PI * 2 * visualSlot / CarouselAmmunitionRackBlockEntity.CAPACITY;
		return this.centerY + (int) Math.round(Math.sin(angle) * RADIUS) - SLOT_SIZE / 2;
	}

	@Override protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.refreshFromWorld();
		CreateGuiHelper.panel(graphics, this.guiLeft, this.guiTop, this.windowWidth, this.windowHeight);
		graphics.drawString(this.font, this.title, this.guiLeft + 12, this.guiTop + 9, CreateGuiHelper.TEXT, false);
		graphics.drawCenteredString(this.font,
			Component.translatable("screen.cbc_firepower_components.carousel_ammunition_rack.output"),
			this.centerX, this.guiTop + 29, 0xFFE7A557);
		for (int i = 0; i < CarouselAmmunitionRackBlockEntity.CAPACITY; ++i) this.renderSlot(graphics, i);
		graphics.drawCenteredString(this.font,
			Component.translatable("screen.cbc_firepower_components.carousel_ammunition_rack.help"),
			this.centerX, this.centerY - 4, CreateGuiHelper.HINT);
		int hovered = this.slotAt(mouseX, mouseY);
		if (hovered >= 0) {
			var pair = this.slots.get(hovered);
			ItemStack tooltip = pair.projectile().isEmpty() ? pair.propellant() : pair.projectile();
			if (!tooltip.isEmpty()) graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
		}
	}

	private void refreshFromWorld() {
		var level = Minecraft.getInstance().level;
		if (level == null || level.getGameTime() == this.lastSyncedGameTime)
			return;
		this.lastSyncedGameTime = level.getGameTime();
		if (!(level.getBlockEntity(this.rackPos) instanceof CarouselAmmunitionRackBlockEntity rack))
			return;
		this.currentIndex = rack.getCurrentIndex();
		this.targetIndex = rack.getTargetIndex();
		this.slots.clear();
		this.slots.addAll(rack.getSlotsSnapshot());
	}

	private void renderSlot(GuiGraphics graphics, int slot) {
		int x = this.slotX(slot), y = this.slotY(slot);
		CreateGuiHelper.slot(graphics, x, y, false);
		if (slot == this.selectedSlot)
			CreateGuiHelper.coloredBorder(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 0xFF70C8E8);
		else if (slot == this.currentIndex)
			CreateGuiHelper.coloredBorder(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 0xFF55C060);
		else if (slot == this.targetIndex)
			CreateGuiHelper.coloredBorder(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 0xFFFFC040);
		var pair = this.slots.get(slot);
		if (!pair.projectile().isEmpty()) graphics.renderItem(pair.projectile(), x + 3, y + 3);
		if (!pair.propellant().isEmpty()) {
			graphics.pose().pushPose();
			graphics.pose().translate(x + 13, y + 13, 200);
			graphics.pose().scale(0.5f, 0.5f, 0.5f);
			graphics.renderItem(pair.propellant(), 0, 0);
			graphics.pose().popPose();
		}
	}
}
