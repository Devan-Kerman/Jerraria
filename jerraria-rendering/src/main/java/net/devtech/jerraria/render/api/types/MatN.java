package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.util.math.Mat;

public abstract class MatN<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
	final DataType matType;

	protected MatN(GlData data, GlValue next, String name, DataType matType) {
		super(data, next, name);
		this.matType = matType;
	}

	public N matN(Mat mat) {
		mat.upload(this.data.element(this.element), this.matType.m, this.matType.n);
		return this.getNext();
	}
}
