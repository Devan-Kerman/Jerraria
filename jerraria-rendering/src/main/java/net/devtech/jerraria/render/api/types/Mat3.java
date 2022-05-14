package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.util.math.Matrix3f;

/**
 * A 3xn matrix value.
 * @see AbstractGlValue
 */
public abstract class Mat3<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
	protected Mat3(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	/**
	 * A 3x3 matrix vertex attribute or uniform
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> Type<x3<N>> mat3(String name) {
		return mat3(name, null);
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	public static <N extends GlValue<?>> Type<x3<N>> mat3(String name, String groupName) {
		return simple(x3::new, DataType.MAT3, name, groupName);
	}

	public static class x3<N extends GlValue<?>> extends Mat3<N> {
		protected x3(GlData data, GlValue next, String name) {
			super(data, next, name);
		}

		public N mat(Matrix3f mat) {
			mat.upload(this.data.element(this.element));
			return this.getNext();
		}

		public N identity() {
			this.data.element(this.element)
			         .f(1).f(0).f(0)
			         .f(0).f(1).f(0)
			         .f(0).f(0).f(1);
			return this.getNext();
		}
	}
}
