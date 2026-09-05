package com.cbcfirepowercomponents.client;

import net.minecraft.client.gui.GuiGraphics;

final class CreateGuiHelper {
	static final int HEADER_HEIGHT = 24;
	static final int FOOTER_HEIGHT = 28;

	static final int TEXT = 0xFFE8E2D3;
	static final int HINT = 0xFFAAA58F;
	static final int PANEL = 0xFF252B29;
	static final int PANEL_INNER = 0xFF111715;
	static final int FIELD = 0xFF171D1C;
	static final int FIELD_HOVERED = 0xFF202826;
	static final int BORDER = 0xFF0A0E0D;
	static final int EDGE_LIGHT = 0xFF5D655B;
	static final int OLIVE = 0xFF4A5130;
	static final int OLIVE_LIGHT = 0xFF687044;
	static final int BRASS = 0xFFB48731;
	static final int BRASS_LIGHT = 0xFFD0A84D;
	static final int GREEN = 0xFF63A85B;
	static final int AMBER = 0xFFE0A83B;
	static final int CYAN = 0xFF63AAB2;
	static final int RED = 0xFFB54A3C;
	static final int DISABLED = 0xFF555B56;

	private CreateGuiHelper() {}

	static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x + 4, y + 5, x + width + 4, y + height + 5, 0x88000000);
		graphics.fill(x, y, x + width, y + height, BORDER);
		graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, EDGE_LIGHT);
		graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, PANEL);

		graphics.fill(x + 4, y + 4, x + width - 4, y + HEADER_HEIGHT, OLIVE);
		graphics.fill(x + 4, y + 4, x + width - 4, y + 6, OLIVE_LIGHT);
		graphics.fill(x + 4, y + HEADER_HEIGHT, x + width - 4, y + HEADER_HEIGHT + 2, BRASS);

		int footerTop = y + height - FOOTER_HEIGHT;
		graphics.fill(x + 4, y + HEADER_HEIGHT + 2, x + width - 4, footerTop, PANEL_INNER);
		graphics.fill(x + 4, footerTop, x + width - 4, footerTop + 2, BRASS);
		graphics.fill(x + 4, footerTop + 2, x + width - 4, y + height - 4, PANEL);

		rivet(graphics, x + 6, y + 7);
		rivet(graphics, x + width - 8, y + 7);
		rivet(graphics, x + 6, y + height - 9);
		rivet(graphics, x + width - 8, y + height - 9);
	}

	static void section(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x, y, x + width, y + height, BORDER);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, FIELD);
		graphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0xFF39423D);
		graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF1D231F);
	}

	static void slot(GuiGraphics graphics, int x, int y, boolean active) {
		slot(graphics, x, y, 18, active);
	}

	static void slot(GuiGraphics graphics, int x, int y, int size, boolean active) {
		graphics.fill(x, y, x + size, y + size, BORDER);
		graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, active ? BRASS : EDGE_LIGHT);
		graphics.fill(x + 2, y + 2, x + size - 2, y + size - 2, active ? 0xFF342B18 : FIELD);
		graphics.fill(x + 3, y + 3, x + size - 3, y + 4, active ? BRASS_LIGHT : 0xFF303834);
	}

	static void control(GuiGraphics graphics, int x, int y, int width, int height,
						boolean hovered, boolean active) {
		graphics.fill(x, y, x + width, y + height, BORDER);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1,
			active ? (hovered ? OLIVE_LIGHT : OLIVE) : DISABLED);
		graphics.fill(x + 2, y + 2, x + width - 2, y + 3,
			active ? BRASS_LIGHT : EDGE_LIGHT);
	}

	static void field(GuiGraphics graphics, int x, int y, int width, int height, boolean active) {
		graphics.fill(x, y, x + width, y + height, BORDER);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, active ? FIELD_HOVERED : DISABLED);
		graphics.fill(x + 2, y + 2, x + width - 2, y + 3, active ? EDGE_LIGHT : 0xFF454A46);
	}

	static void coloredBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 2, color);
		graphics.fill(x, y + height - 2, x + width, y + height, color);
		graphics.fill(x, y, x + 2, y + height, color);
		graphics.fill(x + width - 2, y, x + width, y + height, color);
	}

	static void lamp(GuiGraphics graphics, int x, int y, int color) {
		graphics.fill(x, y, x + 7, y + 7, BORDER);
		graphics.fill(x + 2, y + 2, x + 5, y + 5, color);
		graphics.fill(x + 2, y + 2, x + 4, y + 3, lighten(color));
	}

	private static void rivet(GuiGraphics graphics, int x, int y) {
		graphics.fill(x, y, x + 3, y + 3, BORDER);
		graphics.fill(x + 1, y + 1, x + 2, y + 2, 0xFFA4A99F);
	}

	private static int lighten(int color) {
		int r = Math.min(255, ((color >> 16) & 0xFF) + 45);
		int g = Math.min(255, ((color >> 8) & 0xFF) + 45);
		int b = Math.min(255, (color & 0xFF) + 45);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
}
