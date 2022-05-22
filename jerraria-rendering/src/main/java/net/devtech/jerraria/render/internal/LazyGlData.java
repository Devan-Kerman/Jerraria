package net.devtech.jerraria.render.internal;

import java.util.function.Function;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.GlData;

public final class LazyGlData extends GlData {
	private final Shader shader;
	private final Function<BareShader, GlData> extract;

	public LazyGlData(
		Shader shader, Function<BareShader, GlData> extract) {
		this.shader = shader;
		this.extract = extract;
	}

	@Override
	public Buf element(Element element) {
		if(element instanceof LazyElement l) {
			element = l.getSelf();
		}
		return extract.apply(shader.getShader()).element(element);
	}

	@Override
	public Element getElement(String name) {
		BareShader shader = this.shader.getShader();
		if(shader != null) {
			return extract.apply(shader).getElement(name);
		} else {
			return new LazyElement(this.shader, name, this.extract);
		}
	}

	public UniformData getUniforms() {
		return this.shader.getShader().uniforms;
	}
}
