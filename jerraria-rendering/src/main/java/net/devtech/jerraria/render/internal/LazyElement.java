package net.devtech.jerraria.render.internal;

import java.util.Objects;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.GlData;

public final class LazyElement implements GlData.Element {
	Shader<?> shader;
	String name;
	GlData.Element value;

	public LazyElement(Shader<?> shader, String name) {
		this.shader = shader;
		this.name = name;
	}

	@Override
	public GlData.Element getSelf() {
		GlData.Element value = this.value;
		if(value == null) {
			this.value = value = Objects.requireNonNull(
				this.shader.getShader().uniforms.getElement(this.name),
				"unable to find element " + this.name
			);
			this.name = null;
			this.shader = null;
		}
		return value;
	}
}
