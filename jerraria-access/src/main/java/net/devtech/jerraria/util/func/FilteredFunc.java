package net.devtech.jerraria.util.func;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.devtech.jerraria.access.Access;
import net.devtech.jerraria.access.func.FuncFinder;
import net.devtech.jerraria.util.asm.ContextExtractingMethodBuilder;
import net.devtech.jerraria.util.asm.ProxyClassFactory;


/**
 * creates stack function that conditionally executes stack function based on some context
 */
public interface FilteredFunc<I, F> extends Function<Function<I, F>, F> {
	/**
	 * To use this, you implement your interface, and for the context you want to filter for, pass it into the getter,
	 * this should give you the function(s) that matched the given filter, which u then execute with the given context
	 */
	@Override
	F apply(Function<I, F> functionGetter);

	static <I> Builder<I> builder(Class<I> type) {
		return new Builder<>(type);
	}

	@SuppressWarnings("unchecked")
	class Builder<I> {
		final Class<I> type;
		List<Context<?, I>> contexts = new ArrayList<>();

		public Builder(Class<I> type) {
			this.type = type;
		}


		record Context<T, I>(FuncFinder finder, Class<T> paramType, int ordinal, Function<T, I> extracter) {}

		/**
		 * Create stack context extracter for stack given parameter
		 */
		public <T> Builder<I> ordinal(FuncFinder finder, Class<T> parameterType, int ordinal, Function<T, I> extracter) {
			this.contexts.add(new Context<>(finder, parameterType, ordinal, extracter));
			return this;
		}

		public <T> Builder<I> first(FuncFinder finder, Class<T> parameterType, Function<T, I> extracter) {
			this.contexts.add(new Context<>(finder, parameterType, 0, extracter));
			return this;
		}

		public Builder<I> ordinal(FuncFinder finder, int ordinal) {
			this.ordinal(finder, this.type, ordinal, Function.identity());
			return this;
		}

		public Builder<I> first(FuncFinder finder) {
			this.ordinal(finder, this.type, 0, Function.identity());
			return this;
		}

		public Builder<I> first(String methodName) {
			return this.first(FuncFinder.byName(methodName));
		}

		public <F> FilteredFunc<I, F> buildInfer(F defaultValue, F... arr) {
			return this.build((Class<F>)arr.getClass().componentType(), defaultValue);
		}

		public <F> FilteredFunc<I, F> build(Class<F> type, F defaultValue) {
			ProxyClassFactory<F> factory = new ProxyClassFactory<>(type);
			int counter = 0;

			Map<String, Function<Object, I>> extracters = new HashMap<>();
			for(var context : this.contexts) {
				String field = "extracter" + Integer.toHexString(counter);
				Method method = context.finder.find(type);
				extracters.put(field, (Function<Object, I>) context.extracter);
				factory.add(new ContextExtractingMethodBuilder(field, "nullFunction", method, context.paramType, context.ordinal));
			}
			return function -> {
				Map<String, Object> params = new HashMap<>();
				params.put("nullFunction", defaultValue);
				extracters.forEach((s, f) -> params.put(s, (Function<?, F>) i -> function.apply(f.apply(i))));
				return factory.init(params);
			};
		}
	}
}
