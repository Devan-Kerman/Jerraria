package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.GlValue;

import net.minecraft.client.render.VertexConsumerProvider;

public abstract class RenderLayer<T extends MinecraftShader<?>> {
	public final T shader;
	public final net.minecraft.client.render.RenderLayer layer;

	/**
	 * @see RenderLayers
	 */
	RenderLayer(T shader, net.minecraft.client.render.RenderLayer layer) {
		this.shader = shader;
		this.layer = layer;
	}

	public static <T extends GlValue<?> & GlValue.Attribute> VertexConsumer<T> from(RenderLayer<? extends MinecraftShader<T>> layer, VertexConsumerProvider provider) {
		return layer.shader.provider(provider.getBuffer(layer.layer));
	}

	public abstract <S extends MinecraftShader<?>> RenderLayer<S> withShader(S shader);
}
