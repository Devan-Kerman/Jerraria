package net.devtech.jerraria.gui.api.icons;

import java.awt.Shape;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.gui.api.shape.ShapeTriangulator;
import net.devtech.jerraria.gui.api.shape.Triangulation;
import net.devtech.jerraria.render.api.element.AutoStrat;

public record ShapeIcon(Triangulation triangulation, int color) implements Icon {
	public ShapeIcon(Shape shape, float flatness, int color) {
		this(new Triangulation(), color);
		ShapeTriangulator.triangulate(shape.getPathIterator(null, flatness), this.triangulation);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		SolidColorShader batch = SolidColorShader.KEYS.getBatch(renderer, AutoStrat.TRIANGLE);
		this.triangulation.forEach((x, y, color) -> batch.vert().vec3f(renderer.mat(), x*this.width(), y*this.height(), 1).argb(color), this.color);
	}

	@Override
	public float width() {
		return this.triangulation.width;
	}

	@Override
	public float height() {
		return this.triangulation.height;
	}
}
