package net.devtech.jerraria.util.func;

import java.util.function.Supplier;

import net.devtech.jerraria.util.Validate;

public interface TSupplier<T> extends Supplier<T> {
	static <T> TSupplier<T> of(TSupplier<T> supplier) {
		return supplier;
	}

	@Override
	default T get() {
		try {
			return this.getOrThrow();
		} catch(Throwable e) {
			throw Validate.rethrow(e);
		}
	}

	T getOrThrow() throws Throwable;

}
