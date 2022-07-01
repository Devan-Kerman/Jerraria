package net.devtech.jerraria.gui.api.themes.x16;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.themes.Theme;
import net.devtech.jerraria.gui.api.widgets.Button;

public class x16BitTheme implements Theme {
	public static final Theme INSTANCE = new x16BitTheme();
	@Override
	public Icon widgetBackground(WidgetRenderer renderer, float width, float height) {
		return new ButtonBackground(width, height, ButtonBackground.State.INVERTED);
	}

	@Override
	public Icon textInput(WidgetRenderer renderer, float width, float height) {
		return new ButtonBackground(width, height, ButtonBackground.State.DEFAULT);
	}

	@Override
	public Button.Settings button(WidgetRenderer renderer, float width, float height) {
		return new Button.Settings(
			new ButtonBackground(width, height, ButtonBackground.State.INVERTED),
			new ButtonBackground(width, height, ButtonBackground.State.HIGHLIGHTED_BUTTON),
			new ButtonBackground(width, height, ButtonBackground.State.DEFAULT)
		);
	}
}
