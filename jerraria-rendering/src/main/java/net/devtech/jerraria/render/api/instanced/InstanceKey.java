package net.devtech.jerraria.render.api.instanced;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.ShaderBuffer;
import net.devtech.jerraria.render.api.basic.GlData;

public interface InstanceKey<T> {
	T block();

	int id();

	void invalidate();

	/**
	 * States the given instance must be checked each frame to ensure it's still alive, if it dies, the instance is invalidated.
	 * @param predicate if any predicate returns false, this instance is {@link #invalidate()}d.
	 */
	void addHeartbeat(Predicate<InstanceKey<T>> predicate);

	boolean isValid();

	// utility methods

	default <U extends GlValue<?> & GlValue.Uniform> U ssbo(Function<T, ShaderBuffer<U>> bufferExtract) {
		return bufferExtract.apply(this.block()).from(this);
	}

	default <U extends GlValue<?> & GlValue.Uniform> U uboA(Function<T, U[]> bufferExtract) {
		return bufferExtract.apply(this.block())[this.id()];
	}

	default <U extends GlValue<?> & GlValue.Uniform> U uboL(Function<T, List<U>> bufferExtract) {
		return bufferExtract.apply(this.block()).get(this.id());
	}

	default void assertValidity() {
		if(!this.isValid()) {
			throw new IllegalStateException(this + " is invalid!");
		}
	}
}
