package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.client.render.api.SCopy;
import net.devtech.jerraria.client.render.api.Shader;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ShaderSource {
	public record ShaderKey(Shader<?> source, Id value) {}
	public record ShaderValue<T extends Shader<?>>(T copied, ShaderConfigurator<T> configurator, AutoBlockLayerInvalidation invalidation) {}
	final Map<ShaderKey, ShaderValue<?>> shaderMap = new HashMap<>();

	public ShaderSource() {
	}

	public <T extends Shader<?> & ShaderConfigurator<? super T>> T computeIfAbsent(Id id, T rootShader, AutoBlockLayerInvalidation invalidation) {
		return this.computeIfAbsent(id, rootShader, rootShader, invalidation);
	}

	public <T extends Shader<?> & ShaderConfigurator<? super T>> T computeIfAbsent(Id id, T rootShader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		return this.computeIfAbsent(id, rootShader, rootShader, copy, invalidation);
	}

	public <T extends Shader<?>> T computeIfAbsent(Id id, ShaderConfigurator<? super T> config, T rootShader, AutoBlockLayerInvalidation invalidation) {
		return this.computeIfAbsent(id, config, rootShader, SCopy.PRESERVE_NEITHER, invalidation);
	}

	public <T extends Shader<?>> T computeIfAbsent(Id id, ShaderConfigurator<? super T> config, T rootShader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		return (T) this.shaderMap.computeIfAbsent(new ShaderKey(rootShader, id), key -> new ShaderValue<>(Shader.copy(rootShader, copy), config, invalidation)).copied;
	}

	@ApiStatus.Internal
	public Set<Map.Entry<ShaderKey, ShaderValue<?>>> keySet() {
		return this.shaderMap.entrySet();
	}

	public interface ShaderConfigurator<T extends Shader<?>> {
		/**
		 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
		 */
		void configureUniforms(Matrix3f chunkRenderMatrix, T shader);
	}
}
