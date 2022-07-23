package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.util.math.MatView;

/**
 * A 2d vector of a primitive gl value
 */
public abstract class Vec2<N extends GlValue<?>> extends AbstractGlValue<N>
	implements GlValue.Attribute, GlValue.Uniform {
	protected Vec2(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	/**
	 * A 2d float vector vertex attribute or uniform
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> Type<F<N>> f(String name) {
		return f(name, null);
	}

	/**
	 * A 2d int vector vertex attribute or uniform
	 */
	public static <N extends GlValue<?>> Type<I<N>> i(String name) {
		return i(name, null);
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	public static <N extends GlValue<?>> Type<F<N>> f(String name, String groupName) {
		return simple(F::new, DataType.F32_VEC2, name, groupName);
	}

	public static <N extends GlValue<?>> Type<I<N>> i(String name, String groupName) {
		return simple(I::new, DataType.I32_VEC2, name, groupName);
	}

	public static class F<N extends GlValue<?>> extends Vec2<N> {
		protected F(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec2f(MatView mat, float x, float y) {
			return this.vec2f(mat.mulX(x, y, 1), mat.mulY(x, y, 1));
		}

		public N vec2f(float a, float b) {
			this.data.element(this.element).f(a).f(b);
			return this.getNext();
		}

		public N uv(Texture texture, float u, float v) {
			return this.vec2f(
				texture.getFudgedOffX() + texture.getFudgedWidth() * u,
				texture.getFudgedOffY() + texture.getFudgedHeight() * v
			);
		}
	}

	public static class I<N extends GlValue<?>> extends Vec2<N> {
		protected I(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec2i(int a, int b) {
			this.data.element(this.element).i(a).i(b);
			return this.getNext();
		}

		public N overlay(int uv) {
			return this.vec2i(uv & 0xffff, (uv >> 16) & 0xffff);
		}
	}
}
