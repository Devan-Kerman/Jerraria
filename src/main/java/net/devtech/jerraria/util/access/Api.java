package net.devtech.jerraria.util.access;

import net.devtech.jerraria.util.func.ArrayFunc;

public class Api<F> {
	/**
	 * Low priority, these are for default apis, ones that should be overriden
	 */
	public final RegisterOnlyAccess<F> defaults;
	public final RegisterOnlyAccess<F> access;
	public final ViewOnlyAccess<F> api;

	public Api(ArrayFunc<F> func) {
		this.defaults = Access.create(func);
		this.access = Access.create(func);
		Access<F> api = Access.create(func);
		this.api = api;
		api.dependOn(this.defaults);
		api.dependOn(this.access);
	}
}
