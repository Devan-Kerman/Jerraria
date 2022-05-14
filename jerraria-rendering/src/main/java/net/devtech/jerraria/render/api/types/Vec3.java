package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.util.math.Matrix3f;

/**
 * A 3d vector of a primitive gl value
 */
public abstract class Vec3<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
	/**
	 * A 3d float vector vertex attribute or uniform
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> Type<F<N>> f(String name) {
		return f(name, null);
	}

	/**
	 * A 3d int vector vertex attribute or uniform
	 */
	public static <N extends GlValue<?>> Type<I<N>> i(String name) {
		return i(name, null);
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	public static <N extends GlValue<?>> Type<F<N>> f(String name, String groupName) {
		return simple(F::new, DataType.F32_VEC3, name, groupName);
	}

	public static <N extends GlValue<?>> Type<I<N>> i(String name, String groupName) {
		return simple(I::new, DataType.I32_VEC3, name, groupName);
	}

	protected Vec3(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	public static class F<N extends GlValue<?>> extends Vec3<N> {
		protected F(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec3f(float a, float b, float c) {
			this.data.element(this.element).f(a).f(b).f(c);
			return this.getNext();
		}

		public N vec3f(Matrix3f mat, float a, float b, float c) {
			return this.vec3f(mat.mulX(a, b, c), mat.mulY(a, b, c), mat.mulZ(a, b, c));
		}
	}

	public static class I<N extends GlValue<?>> extends Vec3<N> {
		protected I(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec3i(int a, int b, int c) {
			this.data.element(this.element).i(a).i(b).i(c);
			return this.getNext();
		}
	}
}
