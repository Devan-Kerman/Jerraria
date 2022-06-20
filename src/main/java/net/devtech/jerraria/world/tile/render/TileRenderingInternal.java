package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.internal.batch.BatchRendererImpl;

public class TileRenderingInternal {
	public static BatchRendererImpl impl(BakingChunk chunk) {
		return (BatchRendererImpl) chunk.renderer;
	}
}
