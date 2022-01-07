package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.internal.BufferBuilder;

public interface Shader<T extends ShaderStage> {
	class VertexFormatBuilder<T extends GlValue<?>> {
		final List<GlValue.Type<?>> values;

		public VertexFormatBuilder() {
			this.values = new ArrayList<>();
		}

		public VertexFormatBuilder(VertexFormatBuilder<?> prev, GlValue.Type<T> value) {
			List<GlValue.Type<?>> values = new ArrayList<>(prev.values.size() + 1);
			values.addAll(prev.values);
			values.add(value);
			this.values = values;
		}

		public <N extends GlValue<T>> VertexFormatBuilder<N> add(GlValue.Type<N> type) {
			return new VertexFormatBuilder<>(this, type);
		}

		public UniformBuilder<Primitive<T>> uniforms() {
			return new UniformBuilder<>(this.values);
		}
	}

	T start();

	/**
	 * @return a new version of the shader with it's own buffer
	 */
	Shader<T> copy();

	class UniformBuilder<T extends ShaderStage & ShaderStage.Universal> {
		final List<GlValue.Type<?>> attribute;
		final List<GlValue.Type<?>> uniform;

		public UniformBuilder(List<GlValue.Type<?>> vertex) {
			this.attribute = vertex;
			this.uniform = List.of();
		}

		public UniformBuilder(UniformBuilder<?> prev, GlValue.Type<T> value) {
			List<GlValue.Type<?>> uniform = new ArrayList<>(prev.uniform.size() + 1);
			uniform.addAll(prev.uniform);
			uniform.add(value);
			this.uniform = uniform;
			this.attribute = prev.attribute;
		}

		public <N extends GlValue<T> & ShaderStage.Universal> UniformBuilder<N> add(GlValue.Type<N> type) {
			return new UniformBuilder<>(this, type);
		}

		public Shader<T> build() {
			return null;
		}
	}
}
