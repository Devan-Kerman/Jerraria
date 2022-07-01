package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.gui.api.input.InputState;
import net.devtech.jerraria.gui.api.themes.Theme;
import net.devtech.jerraria.gui.api.themes.x16.x16BitTheme;
import net.devtech.jerraria.util.math.MatView;

public abstract class WidgetRenderer implements MatrixBatchedRenderer {
	Theme currentTheme = x16BitTheme.INSTANCE;

	/**
	 * @return The current drawing matrix, this should be used to transform all vertices
	 */
	@Override
	public abstract MatView mat();

	/**
	 * Add a widget with the given width and height and move the current offset depending on whether we are building up or down.
	 */
	public abstract void drawSpace(float width, float height);

	public abstract float drawSpaceWidth();
	public abstract float drawSpaceHeight();

	public abstract InputState inputState();

	public abstract TextRenderer<?> getTextRenderer();

	/**
	 * @see ImGuiRenderer#setCurrentTheme(Theme)
	 */
	public Theme getCurrentTheme() {
		return this.currentTheme;
	}
}
