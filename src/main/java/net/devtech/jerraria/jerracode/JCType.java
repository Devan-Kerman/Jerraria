package net.devtech.jerraria.jerracode;

import java.util.List;
import java.util.function.Function;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.jerracode.internal.CustomJCType;
import net.devtech.jerraria.jerracode.internal.ListJCType;
import net.devtech.jerraria.jerracode.internal.PairJCType;

public interface JCType<T, N> {
	// todo maybe a "can cache" type method to prevent JCTagBuilder from storing the non-native representation

	static <T, N> JCType<T, N> create(NativeJCType<N> nativeType, Function<T, N> convertToNative, Function<N, T> convertFromNative) {
		return new CustomJCType<>(nativeType, convertToNative, convertFromNative);
	}

	static <T, N> JCType<List<T>, JCList<N>> listOf(JCType<T, N> type) {
		return new ListJCType<>(type);
	}

	NativeJCType<N> nativeType();

	N convertToNative(T value);

	T convertFromNative(N value);

	default <C> JCType<Pair<T, C>, ?> pairType(JCType<C, ?> type) {
		return new PairJCType<>(this, type);
	}
}
