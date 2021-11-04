package net.devtech.jerraria.util.access.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.access.Access;
import net.devtech.jerraria.util.access.AbstractAccess;
import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import net.devtech.jerraria.util.access.ViewOnlyAccess;

@SuppressWarnings("unchecked")
public final class AccessImpl<F> implements Access<F>, AccessInternal<F> {
	final ArrayFunc<F> func;
	private record Dependency(ViewOnlyAccess<?> access, Function<Object, ?> converter) {}
	final List<Object> functions = new ArrayList<>();
	final List<AccessInternal<?>> dependents = new ArrayList<>();

	F combined;

	public AccessImpl(ArrayFunc<F> func) {
		this.func = func;
		this.recompile();
	}

	@Override
	public void notifyRecompile(AccessInternal<?> internal) {
		this.dependents.add(internal);
	}

	@Override
	public void recompile() {
		this.combined = this.getExcept(List.of());
		this.dependents.forEach(AccessInternal::recompile);
	}

	@Override
	public F getExcept(List<AbstractAccess<?>> dependency) {
		List<F> raw = new ArrayList<>();
		for(Object function : this.functions) {
			if(function instanceof Dependency d) {
				var accesses = ImmutableList.<AbstractAccess<?>>builder()
						                           .addAll(dependency)
						                           .add(this)
						                           .build();
				F combined = (F) d.converter.apply(d.access.getExcept(accesses));
				raw.add(combined);
			} else {
				raw.add((F) function);
			}
		}

		F[] fs = raw.toArray(i -> (F[]) Array.newInstance(this.func.getType(), i));
		return this.func.combine(fs);
	}

	@Override
	public void andThen(F function) {
		this.functions.add(function);
		this.recompile();
	}

	@Override
	public void dependOn(AbstractAccess<F> access) {
		this.functions.add(new Dependency((ViewOnlyAccess<?>) access, Function.identity()));
		((AccessInternal<?>)access).notifyRecompile(this);
	}

	@Override
	public <M> void dependOn(AbstractAccess<M> access, Function<M, F> converter) {
		this.functions.add(new Dependency((ViewOnlyAccess<?>) access, (Function<Object, ?>) converter));
		((AccessInternal<?>)access).notifyRecompile(this);
	}

	@Override
	public F get() {
		return this.combined;
	}

	@Override
	public ViewOnlyAccess<F> viewOnly() {
		return new ViewOnlyAccessImpl<>(this);
	}

	/**
	 * shhhh
	 */
	@Override
	public RegisterOnlyAccess<F> registerOnly() {
		return this;
	}
}
