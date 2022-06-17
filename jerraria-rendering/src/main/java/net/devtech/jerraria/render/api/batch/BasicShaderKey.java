package net.devtech.jerraria.render.api.batch;

import java.util.Arrays;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.Shader;

@SuppressWarnings({
	"rawtypes",
	"unchecked"
})
public class BasicShaderKey<T extends Shader<?>> extends ShaderKey<T> {
	private static final BatchConfigurator[] EMPTY = new BatchConfigurator[0];
	final BatchConfigurator<? super T>[] configurators;
	final BuiltGlState state;

	public BasicShaderKey key() {
		return new BasicShaderKey();
	}

	public BasicShaderKey(BatchConfigurator<? super T>[] configurators, BuiltGlState state) {
		this.configurators = configurators;
		this.state = state;
	}

	public BasicShaderKey() {
		this(EMPTY, BuiltGlState.EMPTY);
	}

	public BasicShaderKey<T> withConfig(BatchConfigurator<? super T> configurator) {
		BatchConfigurator<? super T>[] configs = this.configurators;
		BatchConfigurator<? super T>[] copy = Arrays.copyOf(configs, configs.length + 1);
		copy[configs.length] = configurator;
		return new BasicShaderKey<>(copy, this.state);
	}

	public BasicShaderKey<T> withState(BuiltGlState state) {
		return new BasicShaderKey<>(this.configurators, state);
	}

	@Override
	protected void drawKeep(T batch) {
		for(BatchConfigurator<? super T> configurator : this.configurators) {
			configurator.configure(batch);
		}
		batch.drawKeep();
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
