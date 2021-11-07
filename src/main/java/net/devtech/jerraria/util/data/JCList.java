package net.devtech.jerraria.util.data;

import java.util.List;

import com.google.common.collect.ForwardingList;

public class JCList<T> extends ForwardingList<T> {
	final List<T> backing;
	final NativeJCType<T> nativeType;

	public JCList(List<T> backing, NativeJCType<T> type) {
		this.backing = backing;
		this.nativeType = type;
	}

	public NativeJCType<T> getNativeType() {
		return this.nativeType;
	}

	@Override
	protected List<T> delegate() {
		return this.backing;
	}
}
