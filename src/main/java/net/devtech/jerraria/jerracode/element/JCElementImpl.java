package net.devtech.jerraria.jerracode.element;

import net.devtech.jerraria.jerracode.JCType;
import net.devtech.jerraria.jerracode.NativeJCType;

public class JCElementImpl<T> extends AbstractJCElementImpl<T> {
	private final NativeJCType<T> type;
	private final T value;

	JCElementImpl(NativeJCType<T> type, T value) {
		this.type = type;
		this.value = value;
	}

	<C> JCElementImpl(JCType<C, T> type, C value) {
		this(type.nativeType(), type.convertToNative(value));
	}

	@Override
	public NativeJCType<T> type() {return this.type;}

	@Override
	public T value() {return this.value;}
}
