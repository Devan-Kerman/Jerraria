package net.devtech.jerraria.render.api.batch;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.batch.BatchRendererImpl;

public interface BatchedRenderer extends AutoCloseable {
	<T extends Shader<?>> T getBatch(ShaderKey<T> key);

	static BatchedRenderer newInstance() {
		return new BatchRendererImpl();
	}

	@Override
	void close();
}
