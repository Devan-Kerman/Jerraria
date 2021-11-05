package net.devtech.jerraria.util.access.helper;

import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import net.devtech.jerraria.util.access.ViewOnlyAccess;
import net.devtech.jerraria.util.access.priority.PriorityKey;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.func.FilteredFunc;

public interface AccessContext<F> {
	ArrayFunc<F> combiner();

	/**
	 * @see ViewOnlyAccess#access()
	 */
	RegisterOnlyAccess<F> access();

	F emptyFunction();

	default AccessContext<F> withCombiner(ArrayFunc<F> combiner) {
		return new With<>(combiner, this.access(), this.emptyFunction());
	}

	default AccessContext<F> withAccess(RegisterOnlyAccess<F> access) {
		return new With<>(this.combiner(), access, this.emptyFunction());
	}

	default AccessContext<F> withEmpty(F empty) {
		return new With<>(this.combiner(), this.access(), empty);
	}

	default <I> HelperContext<I, F> filter(FilteredFunc<I, F> filter) {
		return new HelperContext.With<>(filter, this.combiner(), this.access(), this.emptyFunction(), PriorityKey.STANDARD);
	}

	record With<F>(ArrayFunc<F> combiner, RegisterOnlyAccess<F> access, F emptyFunction) implements AccessContext<F> {}
}
