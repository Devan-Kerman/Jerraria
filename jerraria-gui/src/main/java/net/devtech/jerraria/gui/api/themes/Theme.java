package net.devtech.jerraria.gui.api.themes;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.widgets.Button;

public interface Theme {
	Icon widgetBackground(WidgetRenderer renderer, float width, float height);

	default Icon textInput(WidgetRenderer renderer, float width, float height) {
		return this.widgetBackground(renderer, width, height);
	}

	default Button.Settings button(WidgetRenderer renderer, float width, float height, String text) {
		Icon textIcon = renderer.getTextRenderer().createIcon(text, 0xFFFFFFFF);
		return this.button(renderer, width, height).withCenter(textIcon, .9f);
	}

	default Button.Settings button(WidgetRenderer renderer, float width, float height) {
		return Button.settings(this.widgetBackground(renderer, width, height));
	}

	default Icon label(WidgetRenderer renderer, float width, float height) {
		return this.widgetBackground(renderer, width, height);
	}
}
