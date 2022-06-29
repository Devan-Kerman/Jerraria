package net.devtech.jerraria.render.api.batch;

import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.batch.BatchRendererImpl;

public interface BatchedRenderer {
	<T extends Shader<?>> T getBatch(ShaderKey<T> key);

	static BatchedRenderer newInstance() {
		return new BatchRendererImpl();
	}

	static BatchedRenderer immediate() {
		return new Immediate();
	}

	default void flush() {}

	void drawKeep(Consumer<Shader<?>> configurator);

	void draw(Consumer<Shader<?>> consumer);

	final class Immediate implements BatchedRenderer {
		Shader<?> last;
		ShaderKey key;

		@Override
		public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
			T instance = key.createInstance();
			if(this.last != null) {
				this.key.drawKeep(this.last);
				this.last.deleteVertexData();
			}
			this.last = instance;
			this.key = key;
			return instance;
		}

		@Override
		public void flush() {
			if(this.last != null) {
				this.key.drawKeep(this.last);
				this.last.deleteVertexData();
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
}
