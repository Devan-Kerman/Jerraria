package net.devtech.jerraria.jerracode.internal;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.devtech.jerraria.jerracode.JCList;
import net.devtech.jerraria.jerracode.JCType;
import net.devtech.jerraria.jerracode.NativeJCType;

public record ListJCType<N, T>(JCType<T, N> type) implements JCType<List<T>, JCList<N>> {
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
}
