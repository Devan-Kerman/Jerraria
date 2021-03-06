package net.devtech.jerraria.access;

import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.access.internal.AccessImpl;

public interface Access<F> extends RegisterOnlyAccess<F>, ViewOnlyAccess<F> {
	static <F> Access<F> create(ArrayFunc<F> func) {
		return new AccessImpl<>(func);
	}

	ViewOnlyAccess<F> viewOnly();

	RegisterOnlyAccess<F> registerOnly();
}
