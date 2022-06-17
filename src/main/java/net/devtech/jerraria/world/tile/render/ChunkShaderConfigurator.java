package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.util.math.Matrix3f;

public interface ChunkShaderConfigurator<T extends Shader<?>> {
	/**
	 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
	 */
	void configureUniforms(Matrix3f chunkRenderMatrix, T shader);
}
