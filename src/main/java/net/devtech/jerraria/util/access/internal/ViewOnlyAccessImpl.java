package net.devtech.jerraria.util.access.internal;

import java.util.List;
import java.util.logging.Logger;

import net.devtech.jerraria.util.access.AbstractAccess;
import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import net.devtech.jerraria.util.func.ArrayFunc;

public record ViewOnlyAccessImpl<F>(AccessImpl<F> backing) implements AccessInternal<F> {
	private static final Logger LOGGER = Logger.getLogger("ViewOnlyAccess");

	@Override
	public void notifyRecompile(AccessInternal<?> internal) {
		this.backing.notifyRecompile(internal);
	}

	@Override
	public void recompile() {
		LOGGER.warning("Recompile called on view-only access, defaulting to calling backing instance!");
		this.backing.recompile();
	}

	@Override
	public F get() {
		return this.backing.get();
	}

	@Override
	public F getExcept(List<AbstractAccess<?>> dependency) {
		return this.backing.getExcept(dependency);
	}

	@Override
	public ArrayFunc<F> combiner() {
		return backing.combiner();
	}

	@Override
	public RegisterOnlyAccess<F> access() {
		throw new UnsupportedOperationException("ViewOnlyAccess cannot be registered to!");
	}

	@Override
	public F emptyFunction() {
		return backing.emptyFunction();
	}
}
