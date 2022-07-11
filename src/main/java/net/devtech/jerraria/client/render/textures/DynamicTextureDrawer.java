package net.devtech.jerraria.client.render.textures;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.math.MatView;

public interface DynamicTextureDrawer {
	/**
	 * Draw the texture to the {@link GLContextState#getDefaultFramebuffer()}
	 * @param view the offset matrix
	 */
	void draw(BatchedRenderer renderer, MatView view);
}
