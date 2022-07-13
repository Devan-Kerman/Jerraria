package net.devtech.jerraria.gui.api.themes.x16;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.EmptyIcon;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.themes.Theme;
import net.devtech.jerraria.gui.api.widgets.Button;

public class x16BitTheme implements Theme {
	public static final Theme INSTANCE = new x16BitTheme();

	@Override
	public Icon widgetBackground(WidgetRenderer renderer, float width, float height) {
		return ButtonType.INVERTED.create(width, height);
	}

	@Override
	public Icon textInput(WidgetRenderer renderer, float width, float height) {
		return ButtonType.DEFAULT.create(width, height);
	}

	@Override
	public Button.Settings button(WidgetRenderer renderer, float width, float height) {
		return new Button.Settings(
			ButtonType.INVERTED.create(width, height),
			ButtonType.HIGHLIGHTED_BUTTON.create(width, height),
			ButtonType.DEFAULT.create(width, height)
		);
	}

	@Override
	public Icon label(WidgetRenderer renderer, float width, float height) {
		return new EmptyIcon(width, height);
	}
}
