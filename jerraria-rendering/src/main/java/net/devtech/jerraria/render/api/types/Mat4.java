package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;

/**
 * A 4xn matrix value.
 * @see AbstractGlValue
 */
public abstract class Mat4<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
	protected Mat4(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	/**
	 * A 4x4 matrix vertex attribute or uniform
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> Type<x4<N>> mat4(String name) {
		return mat4(name, null);
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	public static <N extends GlValue<?>> Type<x4<N>> mat4(String name, String groupName) {
		return simple(x4::new, DataType.MAT4, name, groupName);
	}

	public static class x4<N extends GlValue<?>> extends Mat4<N> {
		protected x4(GlData data, GlValue next, String name) {
			super(data, next, name);
		}

		/*public N mat(Matrix4f mat) {
			// todo matrix4f
			return this.getNext();
		}*/

		public N identity() {
			this.data.element(this.element)
				.f(1).f(0).f(0).f(0)
				.f(0).f(1).f(0).f(0)
				.f(0).f(0).f(1).f(0)
				.f(0).f(0).f(0).f(1);
			return this.getNext();
		}
	}
}
