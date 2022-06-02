package net.devtech.jerraria.render.api;

public interface ShaderBuffer<T extends GlValue<?> & GlValue.Uniform> {
	T getAt(int index);
}
