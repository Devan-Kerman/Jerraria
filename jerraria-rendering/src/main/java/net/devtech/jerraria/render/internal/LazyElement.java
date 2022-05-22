package net.devtech.jerraria.render.internal;

import java.util.Objects;
import java.util.function.Function;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.GlData;

public final class LazyElement implements GlData.Element {
	Shader<?> shader;
	String name;
	GlData.Element value;
	Function<BareShader, GlData> extract;

	public LazyElement(
		Shader<?> shader, String name, Function<BareShader, GlData> extract) {
		this.shader = shader;
		this.name = name;
		this.extract = extract;
	}

	@Override
	public GlData.Element getSelf() {
		GlData.Element value = this.value;
		if(value == null) {
			this.value = value = Objects.requireNonNull(
				this.extract.apply(this.shader.getShader()).getElement(this.name),
				"unable to find element " + this.name
			);
			this.name = null;
			this.shader = null;
			this.extract = null;
		}
		return value;
	}
}
