package net.devtech.jerraria.render.internal;

import java.util.function.Function;

import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.base.GlData;

public final class LazyGlData extends GlData {
	private final ShaderImpl shader;
	private final Function<BareShader, GlData> extract;

	public LazyGlData(
		ShaderImpl shader, Function<BareShader, GlData> extract) {
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

	@Override
	protected void invalidate() throws Exception {
		extract.apply(shader.getShader()).close();
	}

	public UniformData getUniforms() {
		return this.shader.getShader().uniforms;
	}

	@Override
	public GlData getSelf() {
		return extract.apply(shader.getShader());
	}
}
