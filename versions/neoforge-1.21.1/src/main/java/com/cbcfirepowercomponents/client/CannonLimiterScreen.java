package com.cbcfirepowercomponents.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.cannon_limiter.CannonLimiterSettings;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.SetCannonLimiterItemPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class CannonLimiterScreen extends Screen {
	private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath("create", "textures/gui/icons.png");
	private static final int PANEL_MAIN_WIDTH = 244;
	private static final int PANEL_HEIGHT = 170;
	private static final int ROW_GAP = 24;
	private static final int TOGGLE_SIZE = 18;
	private static final int SLIDER_WIDTH = 126;
	private static final int SLIDER_HEIGHT = 18;
	private static final int INPUT_WIDTH = 34;
	private static final float HANDLE_TEXT_SCALE = 0.58f;
	private static final float INPUT_TEXT_SCALE = 0.68f;

	private final InteractionHand hand;
	private final ItemStack stack;
	private final CannonLimiterSettings settings;
	private final List<Row> rows = new ArrayList<>();
	private int panelLeft;
	private int panelTop;

	public CannonLimiterScreen(InteractionHand hand, ItemStack stack) {
		super(Component.translatable("screen.cbc_firepower_components.cannon_limiter"));
		this.hand = hand;
		this.stack = stack;
		this.settings = CannonLimiterSettings.get(stack);
	}

	@Override
	protected void init() {
		this.rows.clear();
		this.panelLeft = this.width / 2 - PANEL_MAIN_WIDTH / 2;
		this.panelTop = this.height / 2 - PANEL_HEIGHT / 2;

		int rowLeft = this.panelLeft + 16;
		int y = this.panelTop + 35;
		this.rows.add(new Row("pitch_min", rowLeft, y, CannonLimiterSettings.MAX_PITCH, true,
			this.settings.hasPitchMin, this.settings.pitchMin));
		this.rows.add(new Row("pitch_max", rowLeft, y + ROW_GAP, CannonLimiterSettings.MAX_PITCH, false,
			this.settings.hasPitchMax, this.settings.pitchMax));
		this.rows.add(new Row("yaw_min", rowLeft, y + ROW_GAP * 2, CannonLimiterSettings.MAX_YAW, true,
			this.settings.hasYawMin, this.settings.yawMin));
		this.rows.add(new Row("yaw_max", rowLeft, y + ROW_GAP * 3, CannonLimiterSettings.MAX_YAW, false,
			this.settings.hasYawMax, this.settings.yawMax));
		for (Row row : this.rows)
			row.addWidgets();

		int buttonY = this.panelTop + PANEL_HEIGHT - 24;
		this.addRenderableWidget(new PanelButton(this.panelLeft + PANEL_MAIN_WIDTH - 54, buttonY, PanelButton.Icon.TRASH,
			Component.translatable("selectWorld.delete"), this::clearSettings));
		this.addRenderableWidget(new PanelButton(this.panelLeft + PANEL_MAIN_WIDTH - 30, buttonY, PanelButton.Icon.CHECK,
			Component.translatable("gui.done"), this::saveAndClose));
	}

	private void clearSettings() {
		for (Row row : this.rows)
			row.clear();
	}

	private void saveAndClose() {
		Row pitchMin = this.rows.get(0);
		Row pitchMax = this.rows.get(1);
		Row yawMin = this.rows.get(2);
		Row yawMax = this.rows.get(3);
		for (Row row : this.rows)
			row.commitInput();
		this.settings.hasPitchMin = pitchMin.enabled;
		this.settings.pitchMin = pitchMin.signedValue();
		this.settings.hasPitchMax = pitchMax.enabled;
		this.settings.pitchMax = pitchMax.signedValue();
		this.settings.hasYawMin = yawMin.enabled;
		this.settings.yawMin = yawMin.signedValue();
		this.settings.hasYawMax = yawMax.enabled;
		this.settings.yawMax = yawMax.signedValue();
		this.settings.normalize();
		if (this.minecraft != null && this.minecraft.player != null)
			CannonLimiterSettings.save(this.minecraft.player.getItemInHand(this.hand), this.settings);
		CannonLimiterSettings.save(this.stack, this.settings);
		MTNetwork.sendToServer(new SetCannonLimiterItemPacket(this.hand, this.settings));
		this.onClose();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0x66000000);
		this.renderFireControlPanel(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.renderItem(this.stack, this.panelLeft + 13, this.panelTop + 4);
		graphics.drawString(this.font, this.title,
			this.panelLeft + PANEL_MAIN_WIDTH / 2 - this.font.width(this.title) / 2, this.panelTop + 9,
			CreateGuiHelper.TEXT, false);
		CreateGuiHelper.lamp(graphics, this.panelLeft + PANEL_MAIN_WIDTH - 19, this.panelTop + 9,
			CreateGuiHelper.AMBER);
		for (Row row : this.rows) {
			if (row.toggle != null && row.toggle.isHovered()) {
				graphics.renderTooltip(this.font, row.toggle.tooltip(), mouseX, mouseY);
				break;
			}
		}
	}

	private void renderFireControlPanel(GuiGraphics graphics) {
		CreateGuiHelper.panel(graphics, this.panelLeft, this.panelTop, PANEL_MAIN_WIDTH, PANEL_HEIGHT);
		CreateGuiHelper.section(graphics, this.panelLeft + 12, this.panelTop + 29, PANEL_MAIN_WIDTH - 24, 104);
		for (int i = 1; i < 4; ++i) {
			int y = this.panelTop + 29 + i * ROW_GAP;
			graphics.fill(this.panelLeft + 14, y, this.panelLeft + PANEL_MAIN_WIDTH - 14, y + 1,
				0xFF303834);
		}
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	private class Row {
		private final String key;
		private final int left;
		private final int top;
		private final float maxAmount;
		private final boolean negative;
		private boolean enabled;
		private float amount;
		private boolean updatingInput;
		private DirectionButton toggle;
		private AngleSlider slider;
		private AngleInput input;

		private Row(String key, int left, int top, float maxAmount, boolean negative, boolean enabled, float signedValue) {
			this.key = key;
			this.left = left;
			this.top = top;
			this.maxAmount = maxAmount;
			this.negative = negative;
			this.enabled = enabled;
			this.amount = Mth.clamp(Math.abs(signedValue), 0.0f, maxAmount);
		}

		private void addWidgets() {
			this.toggle = CannonLimiterScreen.this.addRenderableWidget(new DirectionButton(this.left, this.top - 2,
				this.key, this.toggleText(), () -> {
					this.enabled = !this.enabled;
					this.updateEnabledState();
				}));
			this.slider = CannonLimiterScreen.this.addRenderableWidget(new AngleSlider(this.left + 27, this.top - 2,
				SLIDER_WIDTH, SLIDER_HEIGHT, this));
			this.input = CannonLimiterScreen.this.addRenderableWidget(new AngleInput(CannonLimiterScreen.this.font,
				this.left + 27 + SLIDER_WIDTH + 6, this.top, INPUT_WIDTH, 14, this));
			this.updateInputText();
			this.updateEnabledState();
		}

		private void clear() {
			this.enabled = false;
			this.setAmount(0.0f);
			this.updateEnabledState();
		}

		private void updateEnabledState() {
			this.toggle.setMessage(this.toggleText());
			this.toggle.setEnabledLook(this.enabled);
			this.slider.active = this.enabled;
			this.input.active = this.enabled;
			this.input.setEditable(this.enabled);
		}

		private Component toggleText() {
			return Component.translatable(this.enabled
				? "screen.cbc_firepower_components.cannon_limiter.enabled"
				: "screen.cbc_firepower_components.cannon_limiter.disabled");
		}

		private void setAmount(float amount) {
			this.amount = Mth.clamp(amount, 0.0f, this.maxAmount);
			this.slider.setSliderValue(this.toSliderValue(this.amount));
			this.updateInputText();
		}

		private void setSignedValue(float signedValue) {
			this.setAmount(Math.abs(signedValue));
		}

		private float signedValue() {
			return this.negative ? -this.amount : this.amount;
		}

		private void onInputChanged(String text) {
			if (this.updatingInput)
				return;
			Float parsed = parseAngle(text);
			if (parsed == null)
				return;
			this.setSignedValue(parsed);
		}

		private void commitInput() {
			Float parsed = parseAngle(this.input.getValue());
			if (parsed != null)
				this.setSignedValue(parsed);
			this.updateInputText();
		}

		private void updateInputText() {
			if (this.input == null)
				return;
			this.updatingInput = true;
			this.input.setValue(formatNumber(this.signedValue()));
			this.updatingInput = false;
		}

		private float fromSliderValue(double sliderValue) {
			return (float) (this.maxAmount * sliderValue);
		}

		private double toSliderValue(float amount) {
			return amount / this.maxAmount;
		}
	}

	private static class AngleSlider extends AbstractSliderButton {
		private final Row row;

		private AngleSlider(int x, int y, int width, int height, Row row) {
			super(x, y, width, height, Component.empty(), row.toSliderValue(row.amount));
			this.row = row;
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.empty());
		}

		@Override
		protected void applyValue() {
			this.row.setAmount(this.row.fromSliderValue(this.value));
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int x = this.getX();
			int y = this.getY();
			int trackLeft = x + 2;
			int trackRight = x + this.getWidth() - 2;
			int trackTop = y + 5;
			boolean active = this.active;

			graphics.fill(trackLeft, trackTop, trackRight, trackTop + 8, CreateGuiHelper.BORDER);
			graphics.fill(trackLeft + 1, trackTop + 1, trackRight - 1, trackTop + 7,
				active ? CreateGuiHelper.FIELD : CreateGuiHelper.DISABLED);
			if (active) {
				int fillRight = trackLeft + (int) ((trackRight - trackLeft) * this.value);
				graphics.fill(trackLeft + 2, trackTop + 2, fillRight, trackTop + 6, CreateGuiHelper.GREEN);
			} else {
				graphics.fill(trackLeft + 1, trackTop + 1, trackRight - 1, trackTop + 7,
					CreateGuiHelper.DISABLED);
			}

			Font font = Minecraft.getInstance().font;
			String text = formatHandleAngle(this.row.signedValue());
			int textWidth = Mth.ceil(font.width(text) * HANDLE_TEXT_SCALE);
			int handleWidth = 22;
			int handleX = Mth.clamp(trackLeft + (int) ((trackRight - trackLeft) * this.value) - handleWidth / 2,
				x + 3, x + this.getWidth() - handleWidth - 3);
			CreateGuiHelper.control(graphics, handleX, y + 2, handleWidth, 14,
				this.isHovered(), active);
			int textX = handleX + handleWidth / 2 - textWidth / 2;
			int textY = y + 6;
			graphics.pose().pushPose();
			graphics.pose().translate(textX, textY, 0);
			graphics.pose().scale(HANDLE_TEXT_SCALE, HANDLE_TEXT_SCALE, 1.0f);
			graphics.drawString(font, text, 0, 0, active ? CreateGuiHelper.TEXT : 0xFFB0B3AE, false);
			graphics.pose().popPose();
		}

		private void setSliderValue(double value) {
			this.value = value;
			this.updateMessage();
		}
	}

	private static class DirectionButton extends AbstractWidget {
		private final String key;
		private final Runnable onPress;
		private boolean enabledLook;

		private DirectionButton(int x, int y, String key, Component message, Runnable onPress) {
			super(x, y, TOGGLE_SIZE, TOGGLE_SIZE, message);
			this.key = key;
			this.onPress = onPress;
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			this.onPress.run();
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			CreateGuiHelper.control(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
				this.isHovered(), this.enabledLook);
			int[] icon = iconForKey(this.key);
			graphics.blit(ICONS, this.getX() + 1, this.getY() + 1, icon[0], icon[1], 16, 16);
		}

		private void setEnabledLook(boolean enabledLook) {
			this.enabledLook = enabledLook;
		}

		private Component tooltip() {
			return Component.translatable("screen.cbc_firepower_components.cannon_limiter." + this.key + ".tooltip");
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
		}
	}

	private class PanelButton extends AbstractWidget {
		private enum Icon {
			TRASH,
			CHECK
		}

		private final Icon icon;
		private final Runnable onPress;

		private PanelButton(int x, int y, Icon icon, Component message, Runnable onPress) {
			super(x, y, 18, 18, message);
			this.icon = icon;
			this.onPress = onPress;
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			this.onPress.run();
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			CreateGuiHelper.control(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
				this.isHovered(), true);
			int iconX = this.icon == Icon.CHECK ? 0 : 16;
			int iconY = this.icon == Icon.CHECK ? 16 : 0;
			graphics.blit(ICONS, this.getX() + 1, this.getY() + 1, iconX, iconY, 16, 16);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
		}
	}

	private class AngleInput extends EditBox {
		private final Row row;

		private AngleInput(Font font, int x, int y, int width, int height, Row row) {
			super(font, x, y, width, height, Component.empty());
			this.row = row;
			this.setBordered(false);
			this.setMaxLength(7);
			this.setResponder(row::onInputChanged);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			CreateGuiHelper.field(graphics, this.getX() - 1, this.getY() - 2,
				this.getWidth() + 2, this.getHeight() + 4, this.active);
			Font font = CannonLimiterScreen.this.font;
			String text = this.getValue();
			int color = this.active ? CreateGuiHelper.TEXT : 0xFFB0B3AE;
			int textWidth = Mth.ceil(font.width(text) * INPUT_TEXT_SCALE);
			int textHeight = Mth.ceil(font.lineHeight * INPUT_TEXT_SCALE);
			int textX = this.getX() + this.getWidth() / 2 - textWidth / 2;
			int textY = this.getY() + this.getHeight() / 2 - textHeight / 2 + 1;
			graphics.pose().pushPose();
			graphics.pose().translate(textX, textY, 0);
			graphics.pose().scale(INPUT_TEXT_SCALE, INPUT_TEXT_SCALE, 1.0f);
			graphics.drawString(font, text, 0, 0, color, false);
			graphics.pose().popPose();
			if (this.isFocused() && this.active && (System.currentTimeMillis() / 300L & 1L) == 0L) {
				int cursorX = textX + textWidth;
				graphics.fill(cursorX, textY, cursorX + 1, textY + textHeight, 0xFF222222);
			}
		}
	}

	private static int[] iconForKey(String key) {
		if ("pitch_min".equals(key)) {
			return new int[] {80, 0};
		} else if ("pitch_max".equals(key)) {
			return new int[] {96, 0};
		} else if ("yaw_min".equals(key)) {
			return new int[] {0, 144};
		}
		return new int[] {32, 144};
	}

	private static String formatSignedAngle(float value) {
		float rounded = Math.round(value);
		if (Math.abs(value - rounded) < 0.05f)
			return String.format(Locale.ROOT, "%d\u00b0", (int) rounded);
		return String.format(Locale.ROOT, "%.1f\u00b0", value);
	}

	private static String formatHandleAngle(float value) {
		return String.format(Locale.ROOT, "%d\u00b0", Math.round(value));
	}

	private static String formatNumber(float value) {
		float rounded = Math.round(value);
		if (Math.abs(value - rounded) < 0.05f)
			return String.format(Locale.ROOT, "%d", (int) rounded);
		return String.format(Locale.ROOT, "%.1f", value);
	}

	private static Float parseAngle(String text) {
		try {
			String cleaned = text.trim().replace("\u00b0", "");
			if (cleaned.isEmpty() || "-".equals(cleaned) || "+".equals(cleaned))
				return null;
			return Float.parseFloat(cleaned);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private static void fillFrame(GuiGraphics graphics, int x, int y, int width, int height, int border, int fill) {
		graphics.fill(x, y, x + width, y + height, border);
		graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fill);
	}

	private static String formatAngle(float value) {
		float rounded = Math.round(value);
		if (Math.abs(value - rounded) < 0.05f)
			return String.format(Locale.ROOT, "%d°", (int) rounded);
		return String.format(Locale.ROOT, "%.1f°", value);
	}
}
