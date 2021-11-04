package net.devtech.jerraria.util.access.helper;

import java.util.function.Function;

import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.func.FilteredFunc;

public interface HelperContext<I, F> extends AccessContext<F> {
	FilteredFunc<I, F> filtered();

	default void andThen(Function<I, F> function) {
		F apply = this.filtered().apply(function);
		this.access().andThen(apply);
	}

	@Override
	default HelperContext<I, F> withCombiner(ArrayFunc<F> combiner) {
		return new With<>(this.filtered(), combiner, this.access(), this.emptyFunction());
	}

	@Override
	default HelperContext<I, F> withAccess(RegisterOnlyAccess<F> access) {
		return new With<>(this.filtered(), this.combiner(), access, this.emptyFunction());
	}

	@Override
	default HelperContext<I, F> withEmpty(F empty) {
		return new With<>(this.filtered(), this.combiner(), this.access(), empty);
	}

	default HelperContext<I, F> withFilter(FilteredFunc<I, F> filter) {
		return new With<>(filter, this.combiner(), this.access(), this.emptyFunction());
	}

	default <T> HelperContext<T, F> map(Function<I, T> function) {
		return new With<T, F>(getter -> this.filtered().apply(i -> getter.apply(function.apply(i))), this.combiner(), this.access(), this.emptyFunction());
	}


	record With<I, F>(FilteredFunc<I, F> filtered, ArrayFunc<F> combiner, RegisterOnlyAccess<F> access, F emptyFunction) implements HelperContext<I, F> {}
}
