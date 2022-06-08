package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.api.instanced.InstanceKey;

/**
 * A wrapper for variable sized SSBO arrays (can also do fixed size) {@link Shader#buffer(String, Shader.BufferFunction)}
 * @param <T> the element type
 */
public interface ShaderBuffer<T extends GlValue<?> & GlValue.Uniform> {
	T getOrCreate(int index);

	/**
	 * @return the max created instance
	 */
	int getCurrentSize();

	default T from(InstanceKey<?> key) {
		return this.getOrCreate(key.id());
	}
}
