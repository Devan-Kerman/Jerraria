package net.devtech.jerraria.gui.api.themes;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.widgets.Button;

public class ForwardingTheme implements Theme {
	public Theme backing;

	public ForwardingTheme(Theme backing) {
		this.backing = backing;
	}

	@Override
	public Icon widgetBackground(WidgetRenderer renderer, float width, float height) {
		return this.backing.widgetBackground(renderer, width, height);
	}

	@Override
	public Icon textInput(WidgetRenderer renderer, float width, float height) {
		return this.backing.textInput(renderer, width, height);
	}

	@Override
	public Button.Settings button(WidgetRenderer renderer, float width, float height, String text) {
		return this.backing.button(renderer, width, height, text);
	}

	@Override
	public Button.Settings button(WidgetRenderer renderer, float width, float height) {
		return this.backing.button(renderer, width, height);
	}
}
