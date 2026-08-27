package com.cbcfirepowercomponents.client;

import java.util.ArrayList;
import java.util.List;

import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SelectControllerAmmunitionPacket;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ControllerAmmunitionSelectionScreen extends AbstractSimiScreen {
	private static final int COLUMNS = 8;
	private static final int CELL = 34;
	private final BlockPos controllerPos;
	private final List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options;
	private int rows;

	public ControllerAmmunitionSelectionScreen(BlockPos controllerPos,
		List<ReadyAmmunitionCompartmentBlockEntity.RoundType> options) {
		super(Component.translatable("screen.cbc_firepower_components.controller_ammunition.title"));
		this.controllerPos = controllerPos;
		this.options = new ArrayList<>(options);
	}

	@Override protected void init() {
		this.rows = Math.max(1, (this.options.size() + COLUMNS - 1) / COLUMNS);
		this.setWindowSize(COLUMNS * CELL + 28, this.rows * CELL + 62);
		super.init();
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int option = this.optionAt(mouseX, mouseY);
			if (option >= 0) {
				var selected = this.options.get(option);
				MTNetwork.sendToServer(new SelectControllerAmmunitionPacket(
					this.controllerPos, selected.projectile(), selected.propellant()));
				this.onClose();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private int optionAt(double mouseX, double mouseY) {
		int left = this.guiLeft + 14, top = this.guiTop + 32;
		if (mouseX < left || mouseY < top) return -1;
		int column = (int) ((mouseX - left) / CELL);
		int row = (int) ((mouseY - top) / CELL);
		if (column < 0 || column >= COLUMNS || row < 0 || row >= this.rows) return -1;
		int index = row * COLUMNS + column;
		return index < this.options.size() ? index : -1;
	}

	@Override protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		CreateGuiHelper.panel(graphics, this.guiLeft, this.guiTop, this.windowWidth, this.windowHeight);
		graphics.drawString(this.font, this.title, this.guiLeft + 16, this.guiTop + 9, CreateGuiHelper.TEXT, false);
		CreateGuiHelper.lamp(graphics, this.guiLeft + this.windowWidth - 19, this.guiTop + 9,
			CreateGuiHelper.AMBER);
		CreateGuiHelper.section(graphics, this.guiLeft + 10, this.guiTop + 28,
			this.windowWidth - 20, this.rows * CELL + 8);
		for (int i = 0; i < this.options.size(); ++i) this.renderOption(graphics, i);
		int hovered = this.optionAt(mouseX, mouseY);
		if (hovered >= 0) {
			ItemStack stack = this.options.get(hovered).projectile();
			if (!stack.isEmpty()) graphics.renderTooltip(this.font, stack, mouseX, mouseY);
		}
	}

	private void renderOption(GuiGraphics graphics, int index) {
		var option = this.options.get(index);
		int x = this.guiLeft + 14 + index % COLUMNS * CELL;
		int y = this.guiTop + 32 + index / COLUMNS * CELL;
		CreateGuiHelper.slot(graphics, x + 8, y + 4, option.selected());
		if (option.selected())
			CreateGuiHelper.coloredBorder(graphics, x + 8, y + 4, 18, 18, CreateGuiHelper.GREEN);
		graphics.renderItem(option.projectile(), x + 9, y + 5);
		if (!option.propellant().isEmpty()) {
			graphics.pose().pushPose();
			graphics.pose().translate(x + 20, y + 16, 200);
			graphics.pose().scale(0.55f, 0.55f, 0.55f);
			graphics.renderItem(option.propellant(), 0, 0);
			graphics.pose().popPose();
		}
		graphics.drawString(this.font, Integer.toString(option.count()), x + 23, y + 25,
			CreateGuiHelper.TEXT, true);
	}
}
