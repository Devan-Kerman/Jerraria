package net.devtech.jerraria.render.internal.instanced;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.instanced.InstanceDataUploader;
import net.devtech.jerraria.render.api.instanced.Instancer;
import net.devtech.jerraria.render.api.instanced.InstancingShader;
import net.devtech.jerraria.render.api.instanced.StrictInstancer;

public class StrictInstancerImpl<S extends Shader<?> & InstancingShader, T> implements StrictInstancer<S, T> {
	final InstanceDataUploader<S, T> uploader;
	final Instancer<S> instancer;

	public StrictInstancerImpl(
		InstanceDataUploader<S, T> uploader, Instancer<S> instancer) {
		this.uploader = uploader;
		this.instancer = instancer;
	}

	@Override
	public void addInstance(T instanceData) {
		this.instancer.addInstance(this.uploader, instanceData);
	}
}
