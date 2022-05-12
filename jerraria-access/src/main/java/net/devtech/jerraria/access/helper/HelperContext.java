package net.devtech.jerraria.access.helper;

import java.util.function.Function;

import net.devtech.jerraria.access.RegisterOnlyAccess;
import net.devtech.jerraria.access.priority.PriorityKey;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.func.FilteredFunc;

public interface HelperContext<I, F> extends AccessContext<F> {
	FilteredFunc<I, F> filter();

	PriorityKey priority();

	// todo include priority

	default void andThen(Function<I, F> function) {
		F apply = this.filter().apply(function);
		this.access().andThen(this.priority(), apply);
	}

	@Override
	default HelperContext<I, F> withCombiner(ArrayFunc<F> combiner) {
		return new With<>(this.filter(), combiner, this.access(), this.emptyFunction(), this.priority());
	}

	@Override
	default HelperContext<I, F> withAccess(RegisterOnlyAccess<F> access) {
		return new With<>(this.filter(), this.combiner(), access, this.emptyFunction(), this.priority());
	}

	@Override
	default HelperContext<I, F> withEmpty(F empty) {
		return new With<>(this.filter(), this.combiner(), this.access(), empty, this.priority());
	}

	default HelperContext<I, F> withFilter(FilteredFunc<I, F> filter) {
		return new With<>(filter, this.combiner(), this.access(), this.emptyFunction(), this.priority());
	}

	default <T> HelperContext<T, F> map(Function<I, T> function) {
		return new With<T, F>(getter -> this.filter().apply(i -> getter.apply(function.apply(i))), this.combiner(), this.access(), this.emptyFunction(), this.priority());
	}

	default HelperContext<I, F> withPriority(PriorityKey priority) {
		return new With<>(this.filter(), this.combiner(), this.access(), this.emptyFunction(), priority);
	}


	record With<I, F>(FilteredFunc<I, F> filter, ArrayFunc<F> combiner, RegisterOnlyAccess<F> access, F emptyFunction, PriorityKey priority) implements HelperContext<I, F> {}
}
