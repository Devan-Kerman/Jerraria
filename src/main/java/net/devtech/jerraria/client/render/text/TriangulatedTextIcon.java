package net.devtech.jerraria.client.render.text;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.MatView;

public record TriangulatedTextIcon(TriangulatedText text) implements Icon {
	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		this.draw(renderer, this.width(), this.height());
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
		MatView mat = renderer.mat();
		float size = (mat.mulX(0, 0) - mat.mulX(1, 1)) * 4;
		SolidColorShader batch = SolidColorShader.KEYS.getBatch(renderer, AutoStrat.TRIANGLE);
		this.text.forEach((x, y, color) -> batch.vert().vec3f(mat, x * dimX, y * dimY, 1).argb(color), 1 / Math.abs(size));
	}

	@Override
	public float width() {
		return this.text().aspectRatio() * 8;
	}

	@Override
	public float height() {
		return 8;
	}
}
