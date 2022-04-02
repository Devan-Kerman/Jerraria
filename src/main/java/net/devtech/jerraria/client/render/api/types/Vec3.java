package net.devtech.jerraria.client.render.api.types;

import net.devtech.jerraria.client.render.api.GlValue;
import net.devtech.jerraria.client.render.internal.DataType;
import net.devtech.jerraria.client.render.internal.GlData;
import net.devtech.jerraria.util.math.Matrix3f;

public abstract class Vec3<N extends GlValue<?>> extends GlValue<N> implements GlValue.Attribute {
	final String name;
	final GlData.Element element;

	public static <N extends GlValue<?>> GlValue.Type<Vec3.F<N>> f(String name) {
		return simple((data1, next1) -> new F<>(data1, next1, name), DataType.F32_VEC3, name);
	}

	public static <N extends GlValue<?>> GlValue.Type<Vec3.I<N>> i(String name) {
		return simple((data1, next1) -> new I<>(data1, next1, name), DataType.I32_VEC3, name);
	}

	protected Vec3(GlData data, GlValue<?> next, String name) {
		super(data, next);
		this.name = name;
		this.element = data.getElement(this.name);
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
