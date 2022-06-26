package net.devtech.jerraria.gui.api.widgets;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;

public class Button {
	public static Settings settings(Icon icon) {
		return new Settings(icon, icon.highlighted(), icon.darkened());
	}

	public static Settings settings(Icon icon, Icon pressed) {
		return new Settings(icon, icon.highlighted(), pressed);
	}

	public static Settings settings(Icon icon, Icon hovered, Icon pressed) {
		return new Settings(icon, hovered, pressed);
	}

	public static boolean button(WidgetRenderer renderer, float width, float height, Settings settings) {
		renderer.drawSpace(width, height);
		return false;
	}

	public static final class Settings {
		final Icon defaultState;
		final Icon hover;
		final Icon pressedState;

		public Settings(Icon state, Icon hover, Icon pressed) {
			this.defaultState = state;
			this.hover = hover;
			this.pressedState = pressed;
		}
	}
}
