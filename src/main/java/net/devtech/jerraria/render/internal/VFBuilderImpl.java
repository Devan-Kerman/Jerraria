package net.devtech.jerraria.render.internal;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.api.End;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.VFBuilder;

public class VFBuilderImpl<T extends GlValue<?>> implements VFBuilder<T> {
	public final List<GlValue.Type<?>> attributes;

	public VFBuilderImpl() {
		this.attributes = new ArrayList<>();
	}

	public VFBuilderImpl(VFBuilderImpl<?> prev, GlValue.Type<T> value) {
		List<GlValue.Type<?>> values = new ArrayList<>(prev.attributes.size() + 1);
		values.addAll(prev.attributes);
		values.add(value);
		this.attributes = values;
	}

	@Override
	public <N extends GlValue<T>> VFBuilderImpl<N> add(GlValue.Type<N> type) {
		return new VFBuilderImpl<>(this, type);
	}

	public T build(BareShader shader) {
		GlValue<?> start = new End();
		for(GlValue.Type<?> type : this.attributes) {
			start = type.create(shader.vao, start);
		}
		return (T) start;
	}
}
