package net.devtech.jerraria.access;

import java.util.List;

public interface ViewOnlyAccess<F> {
	F get();

	F getExcept(List<AbstractAccess<?>> dependency);
}
