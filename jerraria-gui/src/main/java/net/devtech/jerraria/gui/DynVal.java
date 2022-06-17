package net.devtech.jerraria.gui;

import java.util.function.Function;

/**
 * A dynamically value
 */
public final class DynVal<T> {
	boolean isStatic;
	T value;
	DynVal<?> derive;
	Function mapper;

	public static <T> DynVal<T> val(T value) {
		return new DynVal<>(value);
	}

	public static <T> DynVal<T> from(DynVal<T> src) {
		return new DynVal<>(src);
	}

	public static <I, T> DynVal<T> from(DynVal<I> value, Function<I, T> mapper) {
		return new DynVal<>(value, mapper);
	}

	public DynVal(T value) {
		this.isStatic = true;
		this.value = value;
	}

	/**
	 * Creates a Dynamic Value that pulls its data from the passed instance
	 */
	public DynVal(DynVal<T> val) {
		this.derive = val;
	}

	/**
	 * Creates a Dynamic Value that pulls its data from the passed instance and converts it into a new value
	 */
	public <I> DynVal(DynVal<I> derive, Function<I, T> mapper) {
		this.derive = derive;
		this.mapper = mapper;
	}


	/**
	 * @return return the value of this dynamic value, or the value of it's source, or the mapped value from it's source
	 */
	public T getValue() {
		if(this.isStatic) {
			return this.value;
		} else {
			final DynVal<?> derive = this.derive;
			//noinspection rawtypes
			final Function mapper = this.mapper;
			if(mapper != null) {
				//noinspection unchecked
				return (T) mapper.apply(derive.getValue());
			} else {
				//noinspection unchecked
				return (T) derive.getValue();
			}
		}
	}

	/**
	 * Overwrite the data in this dynamic value to a constant
	 */
	public DynVal<T> set(T value) {
		this.isStatic = true;
		this.value = value;
		this.derive = null;
		this.mapper = null;
		return this;
	}

	/**
	 * Overwrite the data in this dynamic value to another dynamic value
	 */
	public DynVal<T> src(DynVal<T> value) {
		this.isStatic = false;
		this.derive = value;
		this.mapper = null;
		this.value = null;
		return this;
	}

	/**
	 * Overwrite the data in this dynamic value to another dynamic value
	 */
	public <S> DynVal<T> src(DynVal<S> value, Function<S, T> mapper) {
		this.isStatic = false;
		this.derive = value;
		this.mapper = mapper;
		this.value = null;
		return this;
	}

	public DynVal<T> copy() {
		DynVal<T> val = new DynVal<>(null);
		val.isStatic = this.isStatic;
		val.derive = this.derive;
		val.mapper = this.mapper;
		val.value = this.value;
		return val;
	}
}
