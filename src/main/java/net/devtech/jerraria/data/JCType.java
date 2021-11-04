package net.devtech.jerraria.data;

import java.util.function.Function;

public interface JCType<T, N> {
	static <T, N> JCType<T, N> create(NativeJCType<N> nativeType, Function<T, N> convertToNative, Function<N, T> convertFromNative) {
		return new JCType<T, N>() {
			@Override
			public NativeJCType<N> nativeType() {
				return nativeType;
			}

			@Override
			public N convertToNative(T value) {
				return convertToNative.apply(value);
			}

			@Override
			public T convertFromNative(N value) {
				return convertFromNative.apply(value);
			}
		};
	}

	NativeJCType<N> nativeType();

	N convertToNative(T value);

	T convertFromNative(N value);
}
