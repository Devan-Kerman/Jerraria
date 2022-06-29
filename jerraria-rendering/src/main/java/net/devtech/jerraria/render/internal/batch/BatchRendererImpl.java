package net.devtech.jerraria.render.internal.batch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.api.batch.ShaderKey;

@SuppressWarnings("unchecked")
public class BatchRendererImpl implements BatchedRenderer {
	final ConcurrentMap<ShaderKey<?>, Shader<?>> batches = new ConcurrentHashMap<>();

	@Override
	public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
		return (T) this.batches.computeIfAbsent(key, ShaderKey::createInstance);
	}

	@Override
	public void drawKeep(Consumer<Shader<?>> configurator) {
		for(var entry : this.batches.entrySet()) {
			Shader<?> value = entry.getValue();
			configurator.accept(value);
			((ShaderKey) entry.getKey()).drawKeep(value);
		}
	}

	@Override
	public void draw(Consumer<Shader<?>> consumer) {
		this.drawKeep(consumer);
		this.close();
	}

	public void close() {
		for(Shader<?> value : this.batches.values()) {
			value.close();
		}
		this.batches.clear();
	}

	public void bake() {
		for(Shader<?> value : this.batches.values()) {
			if(value.isValid()) {
				value.bake();
			}
		}
	}
}
