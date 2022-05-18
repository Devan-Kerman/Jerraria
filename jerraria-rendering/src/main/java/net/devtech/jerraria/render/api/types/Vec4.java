package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

/**
 * A 3d vector of a primitive gl value
 */
public abstract class Vec4<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
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
		return simple(F::new, DataType.F32_VEC4, name, groupName);
	}

	public static <N extends GlValue<?>> Type<I<N>> i(String name, String groupName) {
		return simple(I::new, DataType.I32_VEC4, name, groupName);
	}

	protected Vec4(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	public static class F<N extends GlValue<?>> extends Vec4<N> {
		Vector4f v4f;

		protected F(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec4f(MatrixStack mat, float x, float y, float z, float w) {
			return this.vec4f(mat.peek().getPositionMatrix(), x, y, z, w);
		}

		public N vec4f(Matrix4f mat, float x, float y, float z, float w) {
			Vector4f v4f = this.v4f;
			if(v4f == null) {
				v4f = this.v4f = new Vector4f();
			}
			v4f.set(x, y, z, w);
			v4f.transform(mat);
			return this.vec4f(v4f.getX(), v4f.getY(), v4f.getZ(), v4f.getW());
		}

		public N vec4f(float a, float b, float c, float d) {
			this.data.element(this.element).f(a).f(b).f(c).f(d);
			return this.getNext();
		}
	}

	public static class I<N extends GlValue<?>> extends Vec4<N> {
		protected I(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N vec4i(int a, int b, int c, int d) {
			this.data.element(this.element).i(a).i(b).i(c).i(d);
			return this.getNext();
		}
	}
}
