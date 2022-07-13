package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;

public record SolidColorIcon(int argb, float width, float height) implements Icon {
	public SolidColorIcon(int argb) {
		this(argb, 1, 1);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		SolidColorShader batch = SolidColorShader.KEYS.getBatch(renderer, AutoStrat.QUADS);
		batch.rect(renderer.mat(), 0, 0, this.width, this.height, this.argb);
	}
}
