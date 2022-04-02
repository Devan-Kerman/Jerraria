package net.devtech.jerraria.world.tile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.devtech.jerraria.jerracode.element.JCElement;

public interface EnumerableProperty<T, E> {
	String getName();

	List<T> values();

	int defaultIndex();

	default int indexOfValue(T value) {
		int i = this.values().indexOf(value);
		if(i == -1) {
			throw new IllegalArgumentException(value + " not in " + this.infoString());
		}
		return i;
	}

	default T defaultValue() {
		return this.values().get(this.defaultIndex());
	}

	JCElement<E> convert(T value);

	T readFrom(JCElement<E> element);

	default String infoString() {
		return this + "[" + this.values().stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]";
	}
}
