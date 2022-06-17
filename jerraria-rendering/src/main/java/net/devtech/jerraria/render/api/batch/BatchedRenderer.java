package net.devtech.jerraria.render.api.batch;

import net.devtech.jerraria.render.api.Shader;

public interface BatchedRenderer {
	<T extends Shader<?>> T getBatch(ShaderKey<T> key);
}
