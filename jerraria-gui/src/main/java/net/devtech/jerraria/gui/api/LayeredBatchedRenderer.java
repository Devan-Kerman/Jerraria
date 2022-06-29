package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatView;

public interface LayeredBatchedRenderer extends BatchedRenderer {
	/**
	 * Raise the drawing matrix by one layer. By default, the widget renderer uses the depth buffer to order gui components,
	 * so by calling the raise method you can increase the current rendering priority. There is no way to set it back
	 * down, in order to maintain the illusion of sequential rendering.
	 */
	void raise();

	MatView mat();

	default LayeredBatchedRenderer withMat(Mat view) {
		return new MatrixBatchedRenderer(this, view);
	}
}
