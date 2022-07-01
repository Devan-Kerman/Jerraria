package net.devtech.jerraria.render.api.batch;

import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;

public final class ImmediateBatchedRenderer implements BatchedRenderer {
	Shader<?> last;
	ShaderKey key;

	@Override
	public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
		if(this.key == key) {
			return (T) this.last;
		}

		T instance = key.createInstance();
		if(this.last != null) {
			this.key.drawKeep(this.last);
			this.last.close();
		}
		this.last = instance;
		this.key = key;
		return instance;
	}

	@Override
	public void flush() {
		if(this.last != null) {
			this.key.drawKeep(this.last);
			this.last.close();
			this.last = null;
			this.key = null;
		}
	}

	@Override
	public void drawKeep(Consumer<Shader<?>> configurator) {
		throw new UnsupportedOperationException("cannot draw & keep immediate batched renderer!");
	}

	@Override
	public void draw(Consumer<Shader<?>> consumer) {
		this.drawKeep(consumer);
	}
}
