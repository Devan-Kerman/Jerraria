package net.devtech.jerraria.util.access;

import java.util.List;

public interface ViewOnlyAccess<F> extends AbstractAccess<F> {
	F get();

	F getExcept(List<AbstractAccess<?>> dependency);

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	RegisterOnlyAccess<F> access() throws UnsupportedOperationException;
}
