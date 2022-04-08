package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.util.math.Matrix3f;

public abstract class Mat3<N extends GlValue<?>> extends GlValue<N> implements GlValue.Attribute {
	final String name;
	final GlData.Element element;
	protected Mat3(GlData data, GlValue next, String name) {
		super(data, next);
		this.name = name;
		this.element = data.getElement(name);
	}

	public static <N extends GlValue<?>> Type<Mat3.x3<N>> mat3(String name) {
		return simple((data1, next1) -> new Mat3.x3<>(data1, next1, name), DataType.MAT3, name);
	}

	public static class x3<N extends GlValue<?>> extends Mat3<N> {
		protected x3(GlData data, GlValue next, String name) {
			super(data, next, name);
		}

		public N mat(Matrix3f mat) {
			mat.upload(this.data.element(this.element));
			return this.getNext();
		}
	}
}
