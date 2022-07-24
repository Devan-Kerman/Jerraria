package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;

public class DefaultVertexConsumer<T extends GlValue<?> & GlValue.Attribute> implements VertexConsumer<T> {
	final Shader<T> shader;

	public DefaultVertexConsumer(Shader<T> shader) {
		this.shader = shader;
	}

	@Override
	public T vert() {
		return this.shader.vert();
	}

	@Override
	public void flush() {

	}
}
