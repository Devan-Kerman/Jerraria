package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.util.math.Matrix3f;

public interface ChunkRenderableShader {
	/**
	 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
	 */
	void setChunkMatrix(Matrix3f chunkRenderMatrix);
}
