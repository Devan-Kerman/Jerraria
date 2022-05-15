package net.devtech.jerraria.render.api;

import java.util.Collection;

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
		BareShader shader = shader.shader;
		if(shader != null) {
			return shader.uniforms.getElement(name);
		} else {
			return new LazyElement(shader, name);
		}
	}

	@Override
	public Collection<? extends Element> allElements() {
		BareShader shader = shader.shader;
		if(shader != null) {
			return shader.uniforms.allElements();
		} else {
			throw new UnsupportedOperationException("Shader not loaded!");
		}
	}
}
