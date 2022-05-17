package net.devtech.jerraria.render.internal;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.LazyElement;
import net.devtech.jerraria.render.internal.UniformData;

public final class LazyUniformData extends GlData {
	private final Shader shader;

	public LazyUniformData(Shader shader) {this.shader = shader;}

	@Override
	public Buf element(Element element) {
		if(element instanceof LazyElement l) {
			element = l.getSelf();
		}
		return shader.getShader().uniforms.element(element);
	}

	@Override
	public Element getElement(String name) {
		BareShader shader = this.shader.getShader();
		if(shader != null) {
			return shader.uniforms.getElement(name);
		} else {
			return new LazyElement(this.shader, name);
		}
	}

	public UniformData getUniforms() {
		return this.shader.getShader().uniforms;
	}
}
