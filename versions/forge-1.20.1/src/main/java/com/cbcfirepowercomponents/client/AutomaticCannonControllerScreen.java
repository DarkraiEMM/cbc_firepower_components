package com.cbcfirepowercomponents.client;

import java.util.List;

import com.cbcfirepowercomponents.network.ControllerHoldInteractionPacket;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SetControllerConfigPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class AutomaticCannonControllerScreen extends AbstractSimiScreen {
	private final BlockPos controllerPos;
	private int fireMode;
	private int coordinationMode;
	private int signalStrength;

	public AutomaticCannonControllerScreen(BlockPos controllerPos, int fireMode, int coordinationMode,
		int signalStrength) {
		super(Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.title"));
		this.controllerPos = controllerPos;
		this.fireMode = Math.max(0, Math.min(2, fireMode));
		this.coordinationMode = Math.max(0, Math.min(1, coordinationMode));
		this.signalStrength = Math.max(1, Math.min(15, signalStrength));
	}

	@Override protected void init() {
		this.setWindowSize(220, 180);
		super.init();
		Label fireSelection = new Label(this.guiLeft + 32, this.guiTop + 48, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput fireInput = new SelectionScrollInput(this.guiLeft + 24, this.guiTop + 43, 172, 18)
			.forOptions(List.of(this.fireModeLabel(0), this.fireModeLabel(1), this.fireModeLabel(2)))
			.titled(Component.translatable(
				"screen.cbc_firepower_components.automatic_cannon_controller.fire_mode"))
			.writingTo(fireSelection)
			.calling(value -> this.fireMode = value)
			.setState(this.fireMode);
		this.addRenderableWidget(fireSelection);
		this.addRenderableWidget(fireInput);

		Label coordinationSelection = new Label(this.guiLeft + 32, this.guiTop + 86, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput coordinationInput = new SelectionScrollInput(this.guiLeft + 24, this.guiTop + 81, 172, 18)
			.forOptions(List.of(this.coordinationLabel(0), this.coordinationLabel(1)))
			.titled(Component.translatable(
				"screen.cbc_firepower_components.automatic_cannon_controller.coordination"))
			.writingTo(coordinationSelection)
			.calling(value -> this.coordinationMode = value)
			.setState(this.coordinationMode);
		this.addRenderableWidget(coordinationSelection);
		this.addRenderableWidget(coordinationInput);

		List<Component> strengths = java.util.stream.IntStream.rangeClosed(1, 15)
			.mapToObj(value -> (Component) Component.literal(Integer.toString(value))).toList();
		Label strengthSelection = new Label(this.guiLeft + 32, this.guiTop + 124, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput strengthInput = new SelectionScrollInput(this.guiLeft + 24, this.guiTop + 119, 172, 18)
			.forOptions(strengths)
			.titled(Component.translatable(
				"screen.cbc_firepower_components.automatic_cannon_controller.signal_strength"))
			.writingTo(strengthSelection)
			.calling(value -> this.signalStrength = value + 1)
			.setState(this.signalStrength - 1);
		this.addRenderableWidget(strengthSelection);
		this.addRenderableWidget(strengthInput);

		this.addRenderableWidget(new ManualFireButton(this.guiLeft + 14, this.guiTop + 154, 58, 18));

		IconButton confirm = new IconButton(this.guiLeft + 192, this.guiTop + 154, AllIcons.I_CONFIRM);
		confirm.withCallback(() -> {
			MTNetwork.sendToServer(new SetControllerConfigPacket(
				this.controllerPos, this.fireMode, this.coordinationMode, this.signalStrength));
			this.onClose();
		});
		confirm.setToolTip(Component.translatable("gui.done"));
		this.addRenderableWidget(confirm);
	}

	private Component fireModeLabel(int mode) {
		String key = mode == 1 ? "burst" : mode == 2 ? "continuous" : "single";
		return Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.mode." + key);
	}

	private Component coordinationLabel(int mode) {
		return Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.coordination."
			+ (mode == 1 ? "salvo" : "polling"));
	}

	@Override protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		CreateGuiHelper.panel(graphics, this.guiLeft, this.guiTop, this.windowWidth, this.windowHeight);
		graphics.drawString(this.font, this.title, this.guiLeft + 16, this.guiTop + 9, CreateGuiHelper.TEXT, false);
		CreateGuiHelper.lamp(graphics, this.guiLeft + this.windowWidth - 19, this.guiTop + 9,
			CreateGuiHelper.GREEN);
		CreateGuiHelper.section(graphics, this.guiLeft + 12, this.guiTop + 28, 196, 36);
		CreateGuiHelper.section(graphics, this.guiLeft + 12, this.guiTop + 66, 196, 36);
		CreateGuiHelper.section(graphics, this.guiLeft + 12, this.guiTop + 104, 196, 36);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.fire_mode"),
			this.guiLeft + 18, this.guiTop + 32, CreateGuiHelper.HINT, false);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.coordination"),
			this.guiLeft + 18, this.guiTop + 70, CreateGuiHelper.HINT, false);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.signal_strength"),
			this.guiLeft + 18, this.guiTop + 108, CreateGuiHelper.HINT, false);
	}

	private class ManualFireButton extends AbstractWidget {
		private ManualFireButton(int x, int y, int width, int height) {
			super(x, y, width, height,
				Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.manual_fire"));
		}

		@Override public void onClick(double mouseX, double mouseY) {
			MTNetwork.sendToServer(new ControllerHoldInteractionPacket(controllerPos, false));
		}

		@Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			CreateGuiHelper.control(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
				this.isHovered(), this.active);
			graphics.drawCenteredString(font, this.getMessage(), this.getX() + this.getWidth() / 2,
				this.getY() + 5, CreateGuiHelper.TEXT);
		}

		@Override protected void updateWidgetNarration(NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}
}
