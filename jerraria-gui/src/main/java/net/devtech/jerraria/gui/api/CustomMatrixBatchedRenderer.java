package net.devtech.jerraria.gui.api;

import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatView;

final class CustomMatrixBatchedRenderer implements MatrixBatchedRenderer {
	final MatrixBatchedRenderer root;
	final Mat mat;

	CustomMatrixBatchedRenderer(MatrixBatchedRenderer root, Mat view) {
		this.root = root;
		this.mat = view;
	}

	@Override
	public void raise() {
		//this.root.raise();
		//this.mat.offset(0, 0, -1/8388608f);
	}

	@Override
	public MatView mat() {
		return this.mat;
	}

	@Override
	public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
		return this.root.getBatch(key);
	}

	@Override
	public void drawKeep(Consumer<Shader<?>> configurator) {
		this.root.drawKeep(configurator);
	}

	@Override
	public void draw(Consumer<Shader<?>> consumer) {
		this.root.draw(consumer);
	}

	@Override
	public void flush() {
		root.flush();
	}

	@Override
	public MatrixBatchedRenderer withMat(Mat view) {
		return new CustomMatrixBatchedRenderer(this.root, view);
	}
}
