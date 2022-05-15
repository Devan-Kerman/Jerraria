package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.LazyElement;

final class LazyUniformData extends GlData {
	private final Shader shader;

	public LazyUniformData(Shader shader) {this.shader = shader;}

	@Override
	public Buf element(Element element) {
		if(element instanceof LazyElement l) {
			element = l.getSelf();
		}
		return shader.shader.uniforms.element(element);
	}

	@Override
	public Element getElement(String name) {
		BareShader shader = this.shader.shader;
		if(shader != null) {
			return shader.uniforms.getElement(name);
		} else {
			return new LazyElement(this.shader, name);
		}
	}
}
