package net.devtech.jerraria.render.api.instanced;

import net.devtech.jerraria.render.api.Shader;

public interface Instancer<S extends Shader<?> & InstancingShader> {
	<T> void addInstance(InstanceDataUploader<S, T> uploader, T data);
}
