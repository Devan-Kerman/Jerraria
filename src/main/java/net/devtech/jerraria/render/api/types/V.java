package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;

public abstract class V<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute {

	public static <N extends GlValue<?>> GlValue.Type<V.F<N>> f(String name) {
		return simple((data1, next1) -> new F<>(data1, next1, name), DataType.F32, name);
	}

	public static <N extends GlValue<?>> GlValue.Type<V.I<N>> i(String name) {
		return simple((data1, next1) -> new I<>(data1, next1, name), DataType.I32, name);
	}

	protected V(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	public static class F<N extends GlValue<?>> extends V<N> {
		protected F(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N f(float a) {
			this.data.element(this.element).f(a);
			return this.getNext();
		}
	}

	public static class I<N extends GlValue<?>> extends V<N> {
		protected I(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N i(int a) {
			this.data.element(this.element).i(a);
			return this.getNext();
		}
	}
}
