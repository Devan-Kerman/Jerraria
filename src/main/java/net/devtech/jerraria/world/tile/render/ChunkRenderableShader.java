package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.util.math.Mat2x3f;

public interface ChunkRenderableShader {
	/**
	 * sets the offset matrix of the chunk and other misc uniforms prior to rendering
	 */
	void setChunkMatrix(Mat2x3f chunkRenderMatrix);
}
