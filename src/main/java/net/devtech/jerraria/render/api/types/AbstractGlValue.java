package net.devtech.jerraria.render.api.types;

import java.util.Objects;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.GlData;

public abstract class AbstractGlValue<N extends GlValue<?>> extends GlValue<N> {
	protected final String name;
	protected final GlData.Element element;
	protected AbstractGlValue(GlData data, GlValue next, String name) {
		super(data, next);
		this.name = name;
		this.element = Objects.requireNonNull(data.getElement(name), name + " not found!");
	}
}
