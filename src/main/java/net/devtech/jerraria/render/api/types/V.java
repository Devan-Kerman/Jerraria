package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;

/**
 * A primitive gl value such as an int or float
 */
public abstract class V<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {

	/**
	 * A single float vertex attribute or uniform
	 *
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> GlValue.Type<V.F<N>> f(String name) {
		return f(name, null);
	}

	/**
	 * A single int vertex attribute or uniform
	 */
	public static <N extends GlValue<?>> GlValue.Type<V.I<N>> i(String name) {
		return i(name, null);
	}

	public static <N extends GlValue<?>> GlValue.Type<V.F<N>> f(String name, String groupName) {
		return simple(F::new, DataType.F32, name, groupName);
	}

	public static <N extends GlValue<?>> GlValue.Type<V.I<N>> i(String name, String groupName) {
		return simple(I::new, DataType.I32, name, groupName);
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
