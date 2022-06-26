package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.impl.SolidColorShader;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatView;

public record OverlayColorIcon(int overlayColor) implements Icon {
	@Override
	public void draw(BatchedRenderer renderer, MatView matrix) {
		Mat copy = matrix.copy();
		copy.offset(0, 0, -.00001f); // move slightly closer to the screen
		SolidColorShader batch = SolidColorShader.KEYS.getBatch(renderer, AutoStrat.QUADS);
		batch.vert().vec3f(copy,0, 0, 1).argb(this.overlayColor);
		batch.vert().vec3f(copy,0, 1, 1).argb(this.overlayColor);
		batch.vert().vec3f(copy,1, 1, 1).argb(this.overlayColor);
		batch.vert().vec3f(copy,1, 0, 1).argb(this.overlayColor);
	}
}
