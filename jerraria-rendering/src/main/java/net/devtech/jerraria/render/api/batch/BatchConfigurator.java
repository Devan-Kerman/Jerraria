package net.devtech.jerraria.render.api.batch;

import net.devtech.jerraria.render.api.Shader;

public interface BatchConfigurator<T extends Shader<?>> {
	void configure(T shader);
}
