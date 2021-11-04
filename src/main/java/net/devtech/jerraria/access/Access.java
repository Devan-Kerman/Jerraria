package net.devtech.jerraria.access;

public interface Access<F> extends RegisterOnlyAccess<F>, ViewOnlyAccess<F> {
	ViewOnlyAccess<F> viewOnly();

	RegisterOnlyAccess<F> registerOnly();
}
