package net.devtech.jerraria.world.tile.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unchecked")
public class ShaderSource {
	final Map<Key, Value<?>> shaderMap = new HashMap<>();

	public ShaderSource() {
	}

	public <T extends Shader<?>> T computeIfAbsent(ShaderKey<T> key) {
		return (T) this.shaderMap.computeIfAbsent(
			new Key(key.shader(), key.id()),
			k -> new Value<>(Shader.copy(key.shader(), key.copy()), key.config(), key.invalidation(), key.primitive())
		).copied;
	}

	@ApiStatus.Internal
	public Set<Map.Entry<Key, Value<?>>> keySet() {
		return this.shaderMap.entrySet();
	}

	public interface ShaderConfigurator<T extends Shader<?>> {
		/**
		 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
		 */
		void configureUniforms(Matrix3f chunkRenderMatrix, T shader);
	}

	@ApiStatus.Internal
	public record Key(Shader<?> source, Id value) {}

	@ApiStatus.Internal
	public record Value<T extends Shader<?>>(T copied,
	                                         ShaderConfigurator<T> configurator,
	                                         AutoBlockLayerInvalidation invalidation,
	                                         DrawMethod primitive) {}
}
