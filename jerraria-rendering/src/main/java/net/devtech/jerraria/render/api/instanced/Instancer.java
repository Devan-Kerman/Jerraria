package net.devtech.jerraria.render.api.instanced;

import java.util.List;
import java.util.function.UnaryOperator;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.instance.SimpleInstancer;

public interface Instancer<T> {
	/**
	 * @param model a shader object with a single instance of the model, this ideally shouldn't be the main instance.
	 * @param relocator the method used to compact instances
	 * @param instancesPerBlock the maximum number of instances per {@link Shader} instance, this is basically unlimited for SSBOs
	 * @see KeyCopying
	 */
	static <T extends Shader<?>> Instancer<T> simple(InstanceRelocator<T> relocator, T model, int instancesPerBlock) {
		return simple(relocator, model, instancesPerBlock, u -> Shader.copy(u, SCopy.PRESERVE_VERTEX_DATA));
	}

	static <T> Instancer<T> simple(InstanceRelocator<T> relocator, T model, int instancesPerBlock, UnaryOperator<T> copier) {
		return new SimpleInstancer<>(relocator, model, instancesPerBlock) {
			@Override
			protected T copy(T model) {
				return copier.apply(model);
			}
		};
	}

	InstanceKey<T> getOrAllocateId();

	interface Block<T> {
		T block();

		int instances();
	}

	List<? extends Block<T>> compactAndGetBlocks();
}
