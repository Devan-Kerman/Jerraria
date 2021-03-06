package net.devtech.jerraria.access.helper;

import net.devtech.jerraria.access.RegisterOnlyAccess;
import net.devtech.jerraria.access.priority.PriorityKey;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.func.FilteredFunc;

/**
 * You don't have to extend this class to make stack helper, it's just stack utility class
 */
public abstract class AbstractAccessHelper<I, F> implements HelperContext<I, F> {
	public final HelperContext<I, F> helper;

	public AbstractAccessHelper(HelperContext<I, F> helper) {
		this.helper = helper;
	}

	@Override
	public ArrayFunc<F> combiner() {
		return this.helper.combiner();
	}

	@Override
	public RegisterOnlyAccess<F> access() {
		return this.helper.access();
	}

	@Override
	public F emptyFunction() {
		return this.helper.emptyFunction();
	}

	@Override
	public FilteredFunc<I, F> filter() {
		return this.helper.filter();
	}

	@Override
	public PriorityKey priority() {
		return this.helper.priority();
	}
}
