package com.cbcfirepowercomponents.client;

import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.gui.GuiGraphics;

final class CreateGuiHelper {
	static final int PANEL = 0xFF3B3026;
	static final int PANEL_INNER = 0xFF171411;
	static final int TEXT = AllGuiTextures.FONT_COLOR;
	static final int HINT = 0xFFB8A990;

	private CreateGuiHelper() {}

	static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x, y, x + width, y + height, 0xFF5B3C22);
		graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFFD39A54);
		graphics.fill(x + 4, y + 4, x + width - 4, y + height - 4, PANEL);
		graphics.fill(x + 7, y + 22, x + width - 7, y + 24, 0xFF8C6239);
		graphics.fill(x + 7, y + 23, x + width - 7, y + height - 7, PANEL_INNER);
	}

	static void slot(GuiGraphics graphics, int x, int y, boolean active) {
		(active ? AllGuiTextures.HOTSLOT_ACTIVE : AllGuiTextures.HOTSLOT).render(graphics, x, y);
	}

	static void coloredBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}
}
