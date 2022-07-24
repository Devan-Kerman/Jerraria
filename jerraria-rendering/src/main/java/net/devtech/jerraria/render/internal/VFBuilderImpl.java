package net.devtech.jerraria.render.internal;

import java.util.List;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.types.End;

public class VFBuilderImpl<T extends GlValue<?>> implements VFBuilder<T> {
	public final List<GlValue.Type<?>> attributes;

	public static VFBuilderImpl<End> create() {
		return new VFBuilderImpl<>();
	}

	public VFBuilderImpl() {
		this.attributes = List.of();
	}

	public VFBuilderImpl(VFBuilderImpl<?> prev, GlValue.Type<T> value) {
		this.attributes = ImmutableList.<GlValue.Type<?>>builder()
			.addAll(prev.attributes)
			.add(value)
			.build();
	}

	@Override
	public <N extends GlValue<T> & GlValue.Attribute> VFBuilderImpl<N> add(GlValue.Type<N> type) {
		type.validateAttribute();
		return new VFBuilderImpl<>(this, type);
	}

	public Pair<T, End> build(GlData data) {
		End end = new End();
		GlValue<?> start = end;
		for(GlValue.Type<?> type : this.attributes) {
			start = type.create(data, start);
		}
		return Pair.of((T) start, end);
	}

	@Override
	public boolean isEmpty() {
		return this.attributes.isEmpty();
	}
}
