package net.devtech.jerraria.render.internal;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;

public class VFBuilderImpl<T extends GlValue<?>> implements VFBuilder<T> {
	public final List<GlValue.Type<?>> attributes;

	public static VFBuilderImpl<End> create() {
		return new VFBuilderImpl<>();
	}
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
	public <N extends GlValue<T> & GlValue.Attribute> VFBuilderImpl<N> add(GlValue.Type<N> type) {
		type.validateAttribute();
		return new VFBuilderImpl<>(this, type);
	}

	public Pair<T, End> build(BareShader shader) {
		End end = new End();
		GlValue<?> start = end;
		for(GlValue.Type<?> type : this.attributes) {
			start = type.create(shader.vao, start);
		}
		return Pair.of((T) start, end);
	}
}
