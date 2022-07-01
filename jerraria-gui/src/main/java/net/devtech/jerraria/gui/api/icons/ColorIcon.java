package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;

public record ColorIcon(int argb) implements Icon {
	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		SolidColorShader batch = SolidColorShader.KEYS.getBatch(renderer, AutoStrat.QUADS);
		batch.rect(renderer.mat(), 0, 0, 1, 1, this.argb);
	}
}
