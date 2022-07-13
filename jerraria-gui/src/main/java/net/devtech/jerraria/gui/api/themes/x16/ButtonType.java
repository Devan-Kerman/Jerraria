package net.devtech.jerraria.gui.api.themes.x16;

import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.icons.SolidColorIcon;
import net.devtech.jerraria.gui.api.icons.borders.Simple3DBorder;

enum ButtonType {
	INVERTED(0xff8b8b8b, Simple3DBorder.INVERTED),
	DEFAULT(0xff8b8b8b, Simple3DBorder.DEFAULT),
	DISABLED(0xff373737, Simple3DBorder.DISABLED),
	// The button in the beacon inventory uses the exact same colors/texture as regular slots, except it's highlight
	// texture is different
	HIGHLIGHTED_BUTTON(0xff7778a0, Simple3DBorder.HIGHLIGHTED_BUTTON);
	final int background;
	final Simple3DBorder.Settings settings;

	ButtonType(int background, Simple3DBorder.Settings settings) {
		this.background = background;
		this.settings = settings;
	}

	public Icon create(float width, float height) {
		SolidColorIcon border = new SolidColorIcon(this.background, width, height);
		return border.bordered(Simple3DBorder.FACTORY, this.settings);
	}
}
