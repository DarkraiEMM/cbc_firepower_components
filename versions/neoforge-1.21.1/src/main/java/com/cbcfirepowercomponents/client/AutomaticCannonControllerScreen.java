package com.cbcfirepowercomponents.client;

import java.util.List;

import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SetControllerConfigPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
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
		this.setWindowSize(238, 238);
		super.init();
		Label fireSelection = new Label(this.guiLeft + 34, this.guiTop + 53, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput fireInput = new SelectionScrollInput(this.guiLeft + 26, this.guiTop + 48, 186, 18)
			.forOptions(List.of(this.fireModeLabel(0), this.fireModeLabel(1), this.fireModeLabel(2)))
			.titled(Component.translatable(
				"screen.cbc_firepower_components.automatic_cannon_controller.fire_mode"))
			.writingTo(fireSelection)
			.calling(value -> this.fireMode = value)
			.setState(this.fireMode);
		this.addRenderableWidget(fireSelection);
		this.addRenderableWidget(fireInput);

		Label coordinationSelection = new Label(this.guiLeft + 34, this.guiTop + 109, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput coordinationInput = new SelectionScrollInput(this.guiLeft + 26, this.guiTop + 104, 186, 18)
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
		Label strengthSelection = new Label(this.guiLeft + 34, this.guiTop + 165, Component.empty())
			.colored(CreateGuiHelper.TEXT);
		ScrollInput strengthInput = new SelectionScrollInput(this.guiLeft + 26, this.guiTop + 160, 186, 18)
			.forOptions(strengths)
			.titled(Component.translatable(
				"screen.cbc_firepower_components.automatic_cannon_controller.signal_strength"))
			.writingTo(strengthSelection)
			.calling(value -> this.signalStrength = value + 1)
			.setState(this.signalStrength - 1);
		this.addRenderableWidget(strengthSelection);
		this.addRenderableWidget(strengthInput);

		IconButton confirm = new IconButton(this.guiLeft + 210, this.guiTop + 212, AllIcons.I_CONFIRM);
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
		graphics.drawString(this.font, this.title, this.guiLeft + 12, this.guiTop + 9, CreateGuiHelper.TEXT, false);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.fire_mode"),
			this.guiLeft + 12, this.guiTop + 31, CreateGuiHelper.HINT, false);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.coordination"),
			this.guiLeft + 12, this.guiTop + 87, CreateGuiHelper.HINT, false);
		graphics.drawString(this.font,
			Component.translatable("screen.cbc_firepower_components.automatic_cannon_controller.signal_strength"),
			this.guiLeft + 12, this.guiTop + 143, CreateGuiHelper.HINT, false);
	}
}
