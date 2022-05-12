package net.devtech.jerraria.render.api;

public interface VFBuilder<T extends GlValue<?>> {
	<N extends GlValue<T> & GlValue.Attribute> VFBuilder<N> add(GlValue.Type<N> type);
}
