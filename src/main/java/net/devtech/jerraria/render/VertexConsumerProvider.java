package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.GlValue;

public interface VertexConsumerProvider {
	<T extends GlValue<?> & GlValue.Attribute> VertexConsumer<T> getConsumer(RenderLayer<? extends MinecraftShader<T>> renderLayer);
}
