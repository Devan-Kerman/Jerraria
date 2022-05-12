package net.devtech.jerraria.access.internal;

import net.devtech.jerraria.access.ViewOnlyAccess;

public interface AccessInternal<F> extends ViewOnlyAccess<F> {
	void notifyRecompile(AccessInternal<?> internal);

	void recompile();
}
