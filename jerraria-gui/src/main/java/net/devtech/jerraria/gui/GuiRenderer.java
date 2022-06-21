package net.devtech.jerraria.gui;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.util.math.MatView;
import net.devtech.jerraria.util.math.Transformable;

public interface GuiRenderer extends BatchedRenderer, Transformable {
	/**
	 * @return the matrix that should be used to transform <b>ALL</b> draws of the passed component.
	 */
	MatView mat();

	// todo utility methods
}
