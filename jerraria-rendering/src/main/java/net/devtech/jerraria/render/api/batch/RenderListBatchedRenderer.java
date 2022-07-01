package net.devtech.jerraria.render.api.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.devtech.jerraria.render.api.Shader;

public class RenderListBatchedRenderer implements BatchedRenderer {
	final List<Entry<?>> entries = new ArrayList<>();
	record Entry<T extends Shader<?>>(ShaderKey<T> key, T shader) {}

	@Override
	public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
		List<Entry<?>> entries = this.entries;
		Entry<?> entry;
		if(entries.isEmpty() || (entry = entries.get(entries.size() - 1)).key != key) {
			entry = new Entry<>(key, key.createInstance());
			this.entries.add(entry);
		}
		return (T) entry.shader;
	}

	@Override
	public void drawKeep(Consumer<Shader<?>> configurator) {
		for(Entry<?> entry : this.entries) {
			configurator.accept(entry.shader);
			this.drawKeep(entry);
		}
	}

	private <T extends Shader<?>> void drawKeep(Entry<T> entry) {
		entry.key.drawKeep(entry.shader);
	}

	@Override
	public void draw(Consumer<Shader<?>> consumer) {
		for(Entry<?> entry : this.entries) {
			consumer.accept(entry.shader);
			this.drawKeep(entry);
			entry.shader.close();
		}
		this.entries.clear();
	}
}
