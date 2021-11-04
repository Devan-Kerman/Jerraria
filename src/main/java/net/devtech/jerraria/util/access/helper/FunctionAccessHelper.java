package net.devtech.jerraria.util.access.helper;

import java.util.function.Consumer;

import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import net.devtech.jerraria.util.access.internal.CompiledFunctionClassValue;
import net.devtech.jerraria.util.access.internal.FunctionCompiler;
import net.devtech.jerraria.util.access.provider.GenericProvider;

/**
 * Aspect-Oriented version of FunctionAccess.
 *
 * @param <T> the type to filter (eg. Block)
 * @param <F> the function type
 */
public class FunctionAccessHelper<T, F> extends AbstractAccessHelper<T, F> {
	protected final MapFilter<T, F> filterStrong, filterWeak, filterExact;
	protected final CompiledFunctionClassValue<F> filterClassExact;
	protected boolean addedDirectImplementation, addedGenericProvider;

	public FunctionAccessHelper(HelperContext<T, F> context) {
		super(context);
		this.filterStrong = new MapFilter<>(this.combiner(), this.emptyFunction(), false);
		this.filterWeak = new MapFilter<>(this.combiner(), this.emptyFunction(), true);
		this.filterExact = new MapFilter<>(this.combiner(), this.emptyFunction(), () -> new MapMaker().weakKeys().makeMap());
		this.filterClassExact = new CompiledFunctionClassValue<>(this.combiner(), this.emptyFunction());
	}

	/**
	 * if {@link T} instance of {@link F}, returns {@code (F) t}.
	 *
	 * @param functionType the exact type of the function, used to filter. This is a type token to allow the helper to differentiate between {@link
	 *        Consumer<Integer>} and {@link Consumer<String>} for example.
	 */
	public FunctionAccessHelper<T, F> forDirectImplementation(TypeToken<F> functionType) {
		if(!this.addedDirectImplementation) {
			this.addedDirectImplementation = true;
			this.andThen(t -> {
				if(functionType.isSupertypeOf(t.getClass())) {
					return (F) t;
				} else {
					return this.emptyFunction();
				}
			});
		}
		return this;
	}

	/**
	 * adds support for {@link GenericProvider}
	 *
	 * @param access the access this function helper
	 * @see GenericProvider
	 */
	@SuppressWarnings("ALL")
	public FunctionAccessHelper<T, F> forGenericProvider() {
		if(!this.addedGenericProvider) {
			this.addedGenericProvider = true;
			this.andThen(t -> {
				F func;
				if(t instanceof GenericProvider<?> g && (func = (F) g.get((RegisterOnlyAccess) this.access())) != null) {
					return func;
				}
				return this.emptyFunction();
			});
		}
		return this;
	}

	/**
	 * The access holds a weak reference to the object, if the incoming object is {@code ==}, then applies the passed function. This is useful for
	 * classes that shouldn't implement {@link Object#equals(Object)}.
	 */
	public FunctionAccessHelper<T, F> forInstanceExact(T instance, F function) {
		if(this.filterExact.add(instance, function)) {
			this.andThen(this.filterExact::get);
		}
		return this;
	}

	/**
	 * The access holds a weak reference to the object, if the incoming object is {@link Object#equals(Object)}, then applies the passed function.
	 * This is useful for classes that don't implement {@link Object#equals(Object)}, as when they are GCed, they're unreachable by the filter
	 * anyways.
	 */
	public FunctionAccessHelper<T, F> forInstanceWeak(T instance, F function) {
		if(this.filterWeak.add(instance, function)) {
			this.andThen(this.filterWeak::get);
		}
		return this;
	}

	/**
	 * Holds a strong reference to the object, if the incoming object is {@link Object#equals(Object)}, then applies the passed function. This is
	 * useful for classes that implement {@link Object#equals(Object)} such as {@link Integer}.
	 *
	 * @see #forInstanceWeak(Object, Object)
	 */
	public FunctionAccessHelper<T, F> forInstanceStrong(T instance, F function) {
		if(this.filterStrong.add(instance, function)) {
			this.andThen(this.filterStrong::get);
		}
		return this;
	}

	/**
	 * If the incoming object's class is equal to the passed class, then it applies the passed function. This is <b>NOT</b> the same as instanceof.
	 * The reason instanceof is not provided, is because it is O(N) access and can't be optimized.
	 */
	public FunctionAccessHelper<T, F> forClassExact(Class<? extends T> instance, F function) {
		FunctionCompiler<F> compiler = this.filterClassExact.get(instance);
		if(compiler.isEmpty()) {
			this.andThen(t -> this.filterClassExact.get(t.getClass()).get());
		}
		compiler.add(function);
		return this;
	}
}
