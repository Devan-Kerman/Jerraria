package net.devtech.jerraria.render.api.batch;

import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.batch.BatchRendererImpl;

public interface BatchedRenderer {
	<T extends Shader<?>> T getBatch(ShaderKey<T> key);

	static BatchedRenderer newInstance() {
		return new BatchRendererImpl();
	}

	static BatchedRenderer immediate() {
		return new ImmediateBatchedRenderer();
	}

	default void flush() {}

	void drawKeep(Consumer<Shader<?>> configurator);

	void draw(Consumer<Shader<?>> consumer);

}
