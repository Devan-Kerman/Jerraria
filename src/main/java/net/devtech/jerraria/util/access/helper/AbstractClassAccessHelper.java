package net.devtech.jerraria.util.access.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;
import net.devtech.jerraria.util.TypeHelper;

public abstract class AbstractClassAccessHelper<T, C extends Type, F> extends AbstractAccessHelper<C, F> {
	protected final MapFilter<Class<?>, F> filterClassExact;
	protected final MapFilter<Class<?>, F> instanceOfSuper, instanceOfInterface;
	protected final MapFilter<ParameterizedType, F> instanceOfGeneric;

	public AbstractClassAccessHelper(HelperContext<C, F> context) {
		super(context);
		this.filterClassExact = new MapFilter<>(this.combiner(), context.emptyFunction());
		this.instanceOfSuper = new MapFilter<>(this.combiner(), context.emptyFunction());
		this.instanceOfInterface = new MapFilter<>(this.combiner(), context.emptyFunction());
		this.instanceOfGeneric = new MapFilter<>(this.combiner(), context.emptyFunction());
	}

	protected abstract C convert(Type type);

	public AbstractClassAccessHelper<T, C, F> forClassExact(Class<? extends T> type, F func) {
		if(this.filterClassExact.add(type, func)) {
			this.andThen(c -> this.filterClassExact.get(TypeHelper.raw(c)));
		}
		return this;
	}

	public AbstractClassAccessHelper<T, C, F> forClass(Class<? super T> type, F func) {
		if(type.isInterface()) {
			if(this.instanceOfInterface.add(type, func)) {
				this.andThen(c -> this.getInterfaceFunction(TypeHelper.raw(c)));
			}
		} else {
			if(this.instanceOfSuper.add(type, func)) {
				this.andThen(c -> {
					Class<? super C> current = (Class<? super C>) c;
					while(current != null) {
						F found = this.instanceOfSuper.get(current);
						if(found != this.emptyFunction()) {
							return found;
						}
						current = current.getSuperclass();
					}
					return this.emptyFunction();
				});
			}
		}
		return this;
	}


	public AbstractClassAccessHelper<T, C, F> forTypeGeneric(TypeToken<? extends T> token, F func) {
		Type type = token.getType();
		if(type instanceof Class c) {
			return this.forClass(c, func);
		} else if(type instanceof ParameterizedType p) {
			return this.forTypeGeneric(p, func);
		} else {
			throw new UnsupportedOperationException("Unknown type " + type);
		}
	}


	public AbstractClassAccessHelper<T, C, F> forTypeGeneric(ParameterizedType type, F func) {
		if(((Class<?>) type.getRawType()).isInterface()) {
			TypeToken<?> token = TypeToken.of(type);
			this.andThen(c -> {
				if(token.isSupertypeOf(c)) {
					return func;
				}
				return this.emptyFunction();
			});
		} else if(this.instanceOfGeneric.add(type, func)) {
			this.andThen(c -> {
				Type current = c;
				while(current != null) {
					if(current instanceof ParameterizedType paramType) {
						F found = this.instanceOfGeneric.get(paramType);
						if(found != this.emptyFunction()) {
							return found;
						}

					} else {
						current = ((Class<?>) current).getSuperclass();
					}
				}
				return this.emptyFunction();
			});
		}
		return this;
	}

	private F getInterfaceFunction(Class<?> c) {
		if(this.instanceOfInterface.size() < 20) {
			for(var function : this.instanceOfInterface.functions()) {
				if(function.getKey().isAssignableFrom(c)) {
					F found = function.getValue();
					if(found != this.emptyFunction()) {
						return found;
					}
				}
			}
		} else {
			for(Class<?> iface : c.getInterfaces()) {
				F found = this.instanceOfInterface.get(iface);
				if(found != this.emptyFunction()) {
					return found;
				} else {
					found = this.getInterfaceFunction(iface);
					if(found != this.emptyFunction()) {
						return found;
					}
				}
			}
		}
		return this.emptyFunction();
	}
}
