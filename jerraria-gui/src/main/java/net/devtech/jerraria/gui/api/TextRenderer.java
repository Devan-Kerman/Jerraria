package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.gui.api.icons.Icon;

public interface TextRenderer<T> {
	Icon createIcon(T text);
}
