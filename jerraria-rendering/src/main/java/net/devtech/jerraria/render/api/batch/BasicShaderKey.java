package net.devtech.jerraria.render.api.batch;

import java.util.Arrays;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;

@SuppressWarnings({
	"rawtypes",
	"unchecked"
})
public class BasicShaderKey<T extends Shader<?>> extends ShaderKey<T> {
	private static final BatchConfigurator[] EMPTY = new BatchConfigurator[0];
	final BatchConfigurator<? super T>[] configurators;
	final BuiltGlState state;
	final T baseInstance;

	public static <T extends Shader<?>> BasicShaderKey<T> key(T baseInstance) {
		return new BasicShaderKey(baseInstance);
	}

	public BasicShaderKey(BatchConfigurator<? super T>[] configurators, BuiltGlState state, T instance) {
		this.configurators = configurators;
		this.state = state;
		baseInstance = instance;
	}

	public BasicShaderKey(T instance) {
		this(EMPTY, BuiltGlState.EMPTY, instance);
	}

	public BasicShaderKey<T> withConfig(BatchConfigurator<? super T> configurator) {
		BatchConfigurator<? super T>[] configs = this.configurators;
		BatchConfigurator<? super T>[] copy = Arrays.copyOf(configs, configs.length + 1);
		copy[configs.length] = configurator;
		return new BasicShaderKey<>(copy, this.state, this.baseInstance);
	}

	public BasicShaderKey<T> withState(BuiltGlState state) {
		return new BasicShaderKey<>(this.configurators, state, this.baseInstance);
	}

	@Override
	public void drawKeep(T batch) {
		for(BatchConfigurator<? super T> configurator : this.configurators) {
			configurator.configure(batch);
		}
		batch.drawKeep();
	}

	@Override
	public T createInstance() {
		return Shader.copy(this.baseInstance, SCopy.PRESERVE_NEITHER);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public int hashCode() {
		return super.identityHashCode();
	}
}
