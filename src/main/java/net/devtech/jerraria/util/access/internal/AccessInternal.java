package net.devtech.jerraria.util.access.internal;

import net.devtech.jerraria.util.access.ViewOnlyAccess;

public interface AccessInternal<F> extends ViewOnlyAccess<F> {
	void notifyRecompile(AccessInternal<?> internal);

	void recompile();
}
