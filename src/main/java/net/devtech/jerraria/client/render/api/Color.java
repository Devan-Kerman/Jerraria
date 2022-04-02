package net.devtech.jerraria.client.render.api;

import net.devtech.jerraria.client.render.internal.DataType;
import net.devtech.jerraria.client.render.internal.GlData;

public abstract class Color<N extends GlValue<?>> extends GlValue<N> implements GlValue.Attribute {
	final String name;
	final GlData.Element element;

	public static <N extends GlValue<?>> GlValue.Type<RGB<N>> rgb(String name) {
		return simple((data1, next1) -> new RGB<>(data1, next1, name), DataType.NORMALIZED_F8_VEC3, name);
	}

	public static <N extends GlValue<?>> GlValue.Type<ARGB<N>> argb(String name) {
		return simple((data1, next1) -> new ARGB<>(data1, next1, name), DataType.NORMALIZED_F8_VEC4, name);
	}

	protected Color(GlData data, GlValue<?> next, String name) {
		super(data, next);
		this.name = name;
		this.element = data.getElement(this.name);
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
