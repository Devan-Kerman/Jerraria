package net.devtech.jerraria.client.render.api;

import net.devtech.jerraria.client.render.internal.DataType;
import net.devtech.jerraria.client.render.internal.GlData;
import net.devtech.jerraria.client.render.textures.Texture;

public abstract class Vec2<N extends GlValue<?>> extends GlValue<N> implements GlValue.Attribute {
	final String name;
	final GlData.Element element;

	public static <N extends GlValue<?>> Type<Vec2.F<N>> f(String name) {
		return simple((data1, next1) -> new F<>(data1, next1, name), DataType.F32_VEC2, name);
	}

	public static <N extends GlValue<?>> Type<Vec2.I<N>> i(String name) {
		return simple((data1, next1) -> new I<>(data1, next1, name), DataType.I32_VEC2, name);
	}

	protected Vec2(GlData data, GlValue<?> next, String name) {
		super(data, next);
		this.name = name;
		this.element = data.getElement(this.name);
	}

	public static class F<N extends GlValue<?>> extends Vec2<N> {
		protected F(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec2f(float a, float b) {
			this.data.element(this.element).f(a).f(b);
			return this.getNext();
		}

		public N uv(Texture texture, float u, float v) {
			return this.vec2f(texture.getOffX() + texture.getWidth() * u, texture.getOffY() + texture.getHeight() * v);
		}
	}

	public static class I<N extends GlValue<?>> extends Vec2<N> {
		protected I(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec3i(int a, int b) {
			this.data.element(this.element).i(a).i(b);
			return this.getNext();
		}
	}
}
