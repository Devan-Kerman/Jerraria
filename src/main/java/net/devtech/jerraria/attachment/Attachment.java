package net.devtech.jerraria.attachment;

import java.util.Optional;
import java.util.function.Function;

public interface Attachment<O, T> {
	T getValue(O object);

	void setValue(O object, T value);

	default T getOrDefault(O object, T default_) {
		T value = this.getValue(object);
		return value == null ? default_ : value;
	}

	default T getOrApply(O object, Function<O, T> func) {
		T value = this.getValue(object);
		return value == null ? func.apply(object) : value;
	}

	default <M> M mapOrNull(O object, Function<T, M> map) {
		T value = this.getValue(object);
		return value == null ? null : map.apply(value);
	}

	default Optional<T> getOpt(O object) {
		return Optional.of(this.getValue(object));
	}

	AttachmentProvider<O, ?> getProvider();
}
