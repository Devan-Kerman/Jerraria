package net.devtech.jerraria.gui.api.widgets;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.input.InputState;
import net.devtech.jerraria.gui.api.input.MouseButton;

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

	/**
	 * Uses {@link Icon#aspectRatio()} to approximate the button size automatically given the height
	 */
	public static boolean button(WidgetRenderer renderer, float height, Settings settings) {
		return button(renderer, settings.defaultState.aspectRatio() * height, height, settings);
	}

	public static boolean button(WidgetRenderer renderer, float width, float height, Settings settings) {
		return toggleButton(renderer, width, height, settings, false);
	}

	public static boolean toggleButton(WidgetRenderer renderer, float height, Settings settings, boolean oldState) {
		return toggleButton(renderer, height * settings.defaultState.aspectRatio(), height, settings, oldState);
	}

	public static boolean toggleButton(WidgetRenderer renderer, float width, float height, Settings settings, boolean oldState) {
		renderer.drawSpace(width, height);
		InputState state = renderer.inputState();
		boolean inBounds = state.isMouseInBounds();
		boolean isPressed = state.isPressed(MouseButton.Standard.LEFT);
		Icon icon;
		if((isPressed && inBounds) || oldState) {
			icon = settings.pressedState;
		} else if(inBounds) {
			icon = settings.hover;
		} else {
			icon = settings.defaultState;
		}

		icon.draw(renderer, width, height);
		boolean released = inBounds && (state.wasPressed(MouseButton.Standard.LEFT) && !isPressed);
		if(released) {
			oldState ^= true;
		}
		return oldState;
	}

	public static boolean button(WidgetRenderer renderer, float width, float height, String text) {
		Settings background = renderer.getCurrentTheme().button(renderer, width, height, text);
		return button(renderer, width, height, background);
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

		public Settings withCenter(Icon icon, float ratio) {
			return new Settings(
				this.defaultState.centered(icon, ratio),
				this.hover.centered(icon, ratio),
				this.pressedState.centered(icon, ratio)
			);
		}
	}
}
