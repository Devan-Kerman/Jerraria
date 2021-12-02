package net.devtech.jerraria.util.data.internal;

import java.util.function.Function;

import net.devtech.jerraria.util.data.JCType;
import net.devtech.jerraria.util.data.NativeJCType;

public record CustomJCType<N, T>(NativeJCType<N> nativeType, Function<T, N> convertToNative, Function<N, T> convertFromNative) implements JCType<T, N> {
	@Override
	public N convertToNative(T value) {
		return convertToNative.apply(value);
	}

	@Override
	public T convertFromNative(N value) {
		return convertFromNative.apply(value);
	}
}
