package net.devtech.jerraria.access.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.devtech.jerraria.access.priority.PriorityKey;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.access.Access;
import net.devtech.jerraria.access.AbstractAccess;
import net.devtech.jerraria.access.RegisterOnlyAccess;
import net.devtech.jerraria.access.ViewOnlyAccess;

@SuppressWarnings("unchecked")
public final class AccessImpl<F> implements Access<F>, AccessInternal<F> {
	final ArrayFunc<F> func;
	final F empty;

	private record Dependency(ViewOnlyAccess<?> access, Function<Object, ?> converter) {}
	final Multimap<PriorityKey, Object> functions = ArrayListMultimap.create();
	final List<AccessInternal<?>> dependents = new ArrayList<>();

	F combined;

	public AccessImpl(ArrayFunc<F> func) {
		this.func = func;
		this.empty = func.empty();
		this.combined = this.empty;
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
		List<PriorityKey> keys = PriorityKey.sort(this.functions.keySet());
		for(PriorityKey key : keys) {
			for(Object function : this.functions.get(key)) {
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
		}

		F[] fs = raw.toArray(i -> (F[]) Array.newInstance(this.func.getType(), i));
		return this.func.combine(fs);
	}

	@Override
	public void andThen(PriorityKey key, F function) {
		this.functions.put(key, function);
		this.recompile();
	}

	@Override
	public void dependOn(PriorityKey key, AbstractAccess<F> access) {
		this.functions.put(key, new Dependency((ViewOnlyAccess<?>) access, Function.identity()));
		((AccessInternal<?>)access).notifyRecompile(this);
	}

	@Override
	public <M> void dependOn(PriorityKey key, AbstractAccess<M> access, Function<M, F> converter) {
		this.functions.put(key, new Dependency((ViewOnlyAccess<?>) access, (Function<Object, ?>) converter));
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

	@Override
	public ArrayFunc<F> combiner() {
		return this.func;
	}

	@Override
	public RegisterOnlyAccess<F> access() {
		return this;
	}

	@Override
	public F emptyFunction() {
		return this.empty;
	}
}
