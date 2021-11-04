package net.devtech.jerraria.util.access;

import java.util.List;

public interface ViewOnlyAccess<F> {
	F get();

	F getExcept(List<AbstractAccess<?>> dependency);
}
