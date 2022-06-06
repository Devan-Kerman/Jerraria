package net.devtech.jerraria.render.internal.arr;

import java.util.List;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.ShaderBuffer;

public record ListShaderBufferImpl<T extends GlValue<?> & GlValue.Uniform>(List<T> arr) implements ShaderBuffer<T> {
	@Override
	public T getOrCreate(int index) {
		return this.arr.get(index);
	}

	@Override
	public int getCurrentSize() {
		return this.arr.size();
	}
}
