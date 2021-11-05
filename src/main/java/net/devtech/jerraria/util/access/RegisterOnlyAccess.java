package net.devtech.jerraria.util.access;

import java.util.function.Function;

import net.devtech.jerraria.util.access.priority.PriorityKey;

public interface RegisterOnlyAccess<F> extends AbstractAccess<F> {
	default void andThen(F function) {
		this.andThen(PriorityKey.STANDARD, function);
	}

	void andThen(PriorityKey key, F function);

	default void dependOn(AbstractAccess<F> access) {
		this.dependOn(PriorityKey.STANDARD, access);
	}

	default <M> void dependOn(AbstractAccess<M> access, Function<M, F> converter) {
		this.dependOn(PriorityKey.STANDARD, access, converter);
	}

	void dependOn(PriorityKey key, AbstractAccess<F> access);

	<M> void dependOn(PriorityKey key, AbstractAccess<M> access, Function<M, F> converter);
}
