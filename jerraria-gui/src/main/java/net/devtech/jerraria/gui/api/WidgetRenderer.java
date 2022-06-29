package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.gui.api.input.InputState;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.util.math.MatView;

public abstract class WidgetRenderer implements LayeredBatchedRenderer {
	/**
	 * @return The current drawing matrix, this should be used to transform all vertices
	 */
	@Override
	public abstract MatView mat();

	public abstract void drawSpace(float width, float height);

	public abstract float drawSpaceWidth();

	public abstract float drawSpaceHeight();

	public abstract InputState inputState();

	@Override
	public abstract void raise();
}
