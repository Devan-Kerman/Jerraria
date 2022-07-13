package net.devtech.jerraria.gui.api.widgets;

import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;

public class Label {
	public static boolean label(WidgetRenderer renderer, String text) {
		Icon icon = renderer.getTextRenderer().createIcon(text, 0xFFFFFFFF);
		return label(renderer, renderer.getCurrentTheme().label(renderer, icon.width(), icon.height()).centered(icon, .9f));
	}

	public static boolean label(WidgetRenderer renderer, Icon icon) {
		return label(renderer, icon.width(), icon.height(), icon);
	}

	public static boolean label(WidgetRenderer renderer, float height, Icon icon) {
		return label(renderer, icon.aspectRatio()*height, height, icon);
	}

	/**
	 * @return True if the label is hovered over the icon
	 */
	public static boolean label(WidgetRenderer renderer, float width, float height, Icon icon) {
		renderer.drawSpace(width, height);
		icon.draw(renderer, width, height);
		return renderer.inputState().isMouseInBounds();
	}
}
