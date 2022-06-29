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
	final BatchConfigurator<? super T>[] post, pre;
	final BuiltGlState state;
	final T baseInstance;

	public static <T extends Shader<?>> BasicShaderKey<T> key(T baseInstance) {
		return new BasicShaderKey(baseInstance);
	}

	public BasicShaderKey(BatchConfigurator<? super T>[] post, BatchConfigurator<? super T>[] pre, BuiltGlState state, T instance) {
		this.post = post;
		this.pre = pre;
		this.state = state;
		this.baseInstance = instance;
	}

	public BasicShaderKey(T instance) {
		this(EMPTY, EMPTY, BuiltGlState.EMPTY, instance);
	}

	public BasicShaderKey<T> withConfig(BatchConfigurator<? super T> configurator) {
		BatchConfigurator<? super T>[] configs = this.post;
		BatchConfigurator<? super T>[] copy = Arrays.copyOf(configs, configs.length + 1);
		copy[configs.length] = configurator;
		return new BasicShaderKey<>(copy, this.pre, this.state, this.baseInstance);
	}

	public BasicShaderKey<T> withPreConfig(BatchConfigurator<? super T> configurator) {
		BatchConfigurator<? super T>[] configs = this.pre;
		BatchConfigurator<? super T>[] copy = Arrays.copyOf(configs, configs.length + 1);
		copy[configs.length] = configurator;
		return new BasicShaderKey<>(this.post, copy, this.state, this.baseInstance);
	}

	public BasicShaderKey<T> withState(BuiltGlState state) {
		return new BasicShaderKey<>(this.post, this.pre, state, this.baseInstance);
	}

	@Override
	public void drawKeep(T batch) {
		for(BatchConfigurator<? super T> configurator : this.post) {
			configurator.configure(batch);
		}
		batch.drawKeep(this.state);
	}

	@Override
	public T createInstance() {
		T copy = Shader.copy(this.baseInstance, SCopy.PRESERVE_NEITHER);
		for(BatchConfigurator<? super T> configurator : this.pre) {
			configurator.configure(copy);
		}
		return copy;
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
