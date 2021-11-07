package net.devtech.jerraria.util.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

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

	static <T, N> JCType<List<T>, JCList<N>> listOf(JCType<T, N> type) {
		return new JCType<>() {
			@Override
			public NativeJCType<JCList<N>> nativeType() {
				return NativeJCType.listAny(type.nativeType());
			}

			@Override
			public JCList<N> convertToNative(List<T> value) {
				ImmutableList.Builder<N> builder = ImmutableList.builder();
				for(T t : value) {
					builder.add(type.convertToNative(t));
				}
				return new JCList<>(builder.build(), type.nativeType());
			}

			@Override
			public List<T> convertFromNative(JCList<N> value) {
				List<T> list = new ArrayList<>();
				for(N n : value) {
					list.add(type.convertFromNative(n));
				}
				return list;
			}
		};
	}

	NativeJCType<N> nativeType();

	N convertToNative(T value);

	T convertFromNative(N value);
}
