package net.devtech.jerraria.util.access;

import java.util.function.Function;

public interface RegisterOnlyAccess<F> extends AbstractAccess<F> {
	void andThen(F function);

	void dependOn(AbstractAccess<F> access);

	<M> void dependOn(AbstractAccess<M> access, Function<M, F> converter);
}
