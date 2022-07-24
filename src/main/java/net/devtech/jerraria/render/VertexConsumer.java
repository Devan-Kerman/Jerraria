package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.GlValue;

public interface VertexConsumer<T extends GlValue<?> & GlValue.Attribute> {
	T vert();

	void flush();
}
