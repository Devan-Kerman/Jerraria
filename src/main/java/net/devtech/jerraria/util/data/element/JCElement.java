package net.devtech.jerraria.util.data.element;

import java.io.DataOutput;
import java.io.IOException;
import java.util.function.BiConsumer;

import net.devtech.jerraria.util.data.JCIO;
import net.devtech.jerraria.util.data.JCType;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.util.data.pool.JCEncodePool;

public interface JCElement<T> {

	static <T, C> JCElement<T> create(JCType<C, T> type, C value) {
		return new JCElementImpl<>(type, value);
	}

	default void write(JCEncodePool pool, DataOutput output) throws IOException {
		JCIO.write(this.type(), pool, output, this.value());
	}

	default <C> C castTo(NativeJCType<C> type) {
		if(type != this.type()) {
			throw new IllegalArgumentException("type mismatch " + type + " " + this.type());
		}
		return (C) this.value();
	}

	NativeJCType<T> type();

	T value();

	default int hashCode0() {
		int result = this.type().hashCode();
		result = 31 * result + this.value().hashCode();
		return result;
	}

	default boolean equals0(Object obj) {
		if(obj == this) {
			return true;
		}
		return obj instanceof JCElement that && this.type().equals(that.type()) && this.value().equals(that.value());
	}

	default String toString0() {
		return "[" + this.type() + ", " + this.value() + ']';
	}
}
