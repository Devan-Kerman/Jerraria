package net.devtech.jerraria.gui.api;

import java.awt.geom.Rectangle2D;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.util.math.MatView;

public abstract class WidgetRenderer implements BatchedRenderer {
	/**
	 * @return The current offset matrix
	 */
	public abstract MatView mat();

	public abstract void drawSpace(float width, float height);
}
