package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.gui.api.icons.Icon;

public interface TextRenderer<T> {
	Icon createIcon(T text, int argb);

	T createText(String string);

	default Icon createIcon(String string, int argb) {
		return this.createIcon(this.createText(string), argb);
	}
}
