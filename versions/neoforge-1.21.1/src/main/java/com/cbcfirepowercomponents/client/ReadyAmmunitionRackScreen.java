package com.cbcfirepowercomponents.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SwapReadyAmmoSlotsPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ReadyAmmunitionRackScreen extends AbstractSimiScreen {
	private static final int COLUMNS = 8;
	private static final int ROWS = 5;
	private static final int SLOT_SIZE = 18;
	private final BlockPos rackPos;
	private final List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots;
	private int selectedSlot = -1;

	public ReadyAmmunitionRackScreen(BlockPos rackPos,
		List<ReadyAmmunitionCompartmentBlockEntity.RoundPair> slots) {
		super(Component.translatable("screen.cbc_firepower_components.ready_ammunition_compartment.title"));
		this.rackPos = rackPos;
		this.slots = new ArrayList<>(slots);
		while (this.slots.size() < ReadyAmmunitionCompartmentBlockEntity.CAPACITY)
			this.slots.add(new ReadyAmmunitionCompartmentBlockEntity.RoundPair(ItemStack.EMPTY, ItemStack.EMPTY));
	}

	@Override protected void init() {
		this.setWindowSize(184, 154);
		super.init();
		IconButton confirm = new IconButton(this.guiLeft + 156, this.guiTop + 128, AllIcons.I_CONFIRM);
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
					MTNetwork.sendToServer(new SwapReadyAmmoSlotsPacket(this.rackPos, this.selectedSlot, slot));
					this.selectedSlot = -1;
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private int slotAt(double mouseX, double mouseY) {
		int left = this.guiLeft + 20, top = this.guiTop + 29;
		if (mouseX < left || mouseY < top) return -1;
		int column = (int) ((mouseX - left) / SLOT_SIZE);
		int row = (int) ((mouseY - top) / SLOT_SIZE);
		if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) return -1;
		return row * COLUMNS + column;
	}

	@Override protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		CreateGuiHelper.panel(graphics, this.guiLeft, this.guiTop, this.windowWidth, this.windowHeight);
		graphics.drawString(this.font, this.title, this.guiLeft + 12, this.guiTop + 9, CreateGuiHelper.TEXT, false);
		for (int i = 0; i < ReadyAmmunitionCompartmentBlockEntity.CAPACITY; ++i) this.renderSlot(graphics, i);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.ready_ammunition_compartment.help"),
			this.guiLeft + 12, this.guiTop + 123, CreateGuiHelper.HINT, false);
		int hovered = this.slotAt(mouseX, mouseY);
		if (hovered >= 0) {
			var pair = this.slots.get(hovered);
			ItemStack tooltip = pair.projectile().isEmpty() ? pair.propellant() : pair.projectile();
			if (!tooltip.isEmpty()) graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
		}
	}

	private void renderSlot(GuiGraphics graphics, int slot) {
		int x = this.guiLeft + 20 + slot % COLUMNS * SLOT_SIZE;
		int y = this.guiTop + 29 + slot / COLUMNS * SLOT_SIZE;
		CreateGuiHelper.slot(graphics, x, y, false);
		if (slot == this.selectedSlot)
			CreateGuiHelper.coloredBorder(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 0xFFFFC040);
		else if (slot == 0)
			CreateGuiHelper.coloredBorder(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 0xFF55C060);
		var pair = this.slots.get(slot);
		if (!pair.projectile().isEmpty()) graphics.renderItem(pair.projectile(), x + 1, y + 1);
		if (!pair.propellant().isEmpty()) {
			graphics.pose().pushPose();
			graphics.pose().translate(x + 10, y + 10, 200);
			graphics.pose().scale(0.5f, 0.5f, 0.5f);
			graphics.renderItem(pair.propellant(), 0, 0);
			graphics.pose().popPose();
		}
	}
}
