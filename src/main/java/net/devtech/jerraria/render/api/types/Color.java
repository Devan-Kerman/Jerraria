package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;

/**
 * A packed RGB or ARGB color that is sent to the GPU as an int for efficiency.
 *
 * For uniform Colors, use an int and https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/unpackUnorm.xhtml in your shader.
 */
public abstract class Color<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute {

	/**
	 * A non-translucent color vertex attribute or uniform
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> GlValue.Type<RGB<N>> rgb(String name) {
		return rgb(name, null);
	}

	/**
	 * A translucent color vertex attribute or uniform, accepts alpha values
	 */
	public static <N extends GlValue<?>> GlValue.Type<ARGB<N>> argb(String name) {
		return argb(name, null);
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	public static <N extends GlValue<?>> GlValue.Type<RGB<N>> rgb(String name, String groupName) {
		return simple(RGB::new, DataType.NORMALIZED_F8_VEC3, name, groupName);
	}

	public static <N extends GlValue<?>> GlValue.Type<ARGB<N>> argb(String name, String groupName) {
		return simple(ARGB::new, DataType.NORMALIZED_F8_VEC4, name, groupName);
	}

	protected Color(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	public static class RGB<N extends GlValue<?>> extends Color<N> {
		protected RGB(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N rgb(int r, int g, int b) {
			this.data.element(this.element).b((byte) r).b((byte) g).b((byte) b);
			return this.getNext();
		}

		public N rgb(int rgb) {
			return this.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
		}
	}

	public static class ARGB<N extends GlValue<?>> extends Color<N> {
		protected ARGB(GlData data, GlValue<?> next, String name) {
			super(data, next, name);
		}

		public N argb(int a, int r, int g, int b) {
			this.data.element(this.element).b((byte) a).b((byte) r).b((byte) g).b((byte) b);
			return this.getNext();
		}

		public N argb(int rgb) {
			return this.argb((rgb >> 24) & 0xFF, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
		}
	}
}
