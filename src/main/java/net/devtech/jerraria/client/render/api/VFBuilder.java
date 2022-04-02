package net.devtech.jerraria.client.render.api;

public interface VFBuilder<T extends GlValue<?>> {
	<N extends GlValue<T>> VFBuilder<N> add(GlValue.Type<N> type);
}
