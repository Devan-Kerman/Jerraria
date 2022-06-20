package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.api.batch.ShaderKey;

@SuppressWarnings("unchecked")
public class BakingChunk {
	final BatchedRenderer renderer;
	AutoBlockLayerInvalidation rebake;

	public BakingChunk() {
		this.renderer = BatchedRenderer.newInstance();
	}

	public <T extends Shader<?> & ChunkRenderableShader> T getBatch(ShaderKey<T> key) {
		return renderer.getBatch(key);
	}

	public void minInvalidation(AutoBlockLayerInvalidation invalidation) {
		if(invalidation.ordinal() < this.rebake.ordinal()) {
			this.rebake = invalidation;
		}
	}

	public AutoBlockLayerInvalidation getMinInvalidation() {
		return this.rebake;
	}
}
