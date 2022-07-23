package net.devtech.jerraria.render.api;

/**
 * VertexFormat builder, call {@link #add(GlValue.Type)} in reverse order of the generics
 */
public interface VFBuilder<T extends GlValue<?>> {
	<N extends GlValue<T> & GlValue.Attribute> VFBuilder<N> add(GlValue.Type<N> type);

	boolean isEmpty();
}
