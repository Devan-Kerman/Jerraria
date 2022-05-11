package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.util.math.Matrix3f;

public abstract class Vec3<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute {
	public static <N extends GlValue<?>> GlValue.Type<Vec3.F<N>> f(String name) {
		return f(name, null);
	}

	public static <N extends GlValue<?>> GlValue.Type<Vec3.I<N>> i(String name) {
		return i(name, null);
	}

	public static <N extends GlValue<?>> GlValue.Type<Vec3.F<N>> f(String name, String groupName) {
		return simple((data1, next1) -> new F<>(data1, next1, name), DataType.F32_VEC3, name, groupName);
	}

	public static <N extends GlValue<?>> GlValue.Type<Vec3.I<N>> i(String name, String groupName) {
		return simple((data1, next1) -> new I<>(data1, next1, name), DataType.I32_VEC3, name, groupName);
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
