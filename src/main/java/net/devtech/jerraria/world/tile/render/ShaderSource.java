package net.devtech.jerraria.world.tile.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unchecked")
public class ShaderSource {
	final Map<ShaderKey<?>, Shader<?>> shaderMap = new HashMap<>();

	public ShaderSource() {
	}

	public <T extends Shader<?>> T computeIfAbsent(ShaderKey<T> key) {
		return (T) this.shaderMap.computeIfAbsent(
			key,
			k -> Shader.copy(key.shader(), key.copy())
		);
	}

	@ApiStatus.Internal
	public Set<Map.Entry<ShaderKey<?>, Shader<?>>> entries() {
		return this.shaderMap.entrySet();
	}

	public void close() {
		RenderThread.queueRenderTask(() -> {
			for(Shader<?> value : this.shaderMap.values()) {
				value.close();
			}
		});
	}

	public interface ShaderConfigurator<T extends Shader<?>> {
		/**
		 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
		 */
		void configureUniforms(Matrix3f chunkRenderMatrix, T shader);
	}
}
