package net.devtech.jerraria.render.api.instanced;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.instanced.StrictInstancerImpl;

public interface StrictInstancer<S extends Shader<?> & InstancingShader, T> {
	static <S extends Shader<?> & InstancingShader, T> StrictInstancer<S, T> create(Instancer<S> instancer, InstanceDataUploader<S, T> uploader) {
		return new StrictInstancerImpl<>(uploader, instancer);
	}

	void addInstance(T instanceData);
}
