package net.devtech.jerraria.world.internal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.AutoBlockLayerInvalidation;
import net.devtech.jerraria.world.tile.render.BakingChunk;
import net.devtech.jerraria.world.tile.render.TileRenderer;
import net.devtech.jerraria.world.tile.render.TileRenderingInternal;

public class ClientChunkBakedTileQuadrantRenderer {
	public static BakingChunk bake(World localWorld, int absQuadrantX, int absQuadrantY) {
		BakingChunk source = new BakingChunk();
		int startX = absQuadrantX << World.LOG2_CHUNK_QUADRANT_SIZE;
		int startY = absQuadrantY << World.LOG2_CHUNK_QUADRANT_SIZE;
		int endX = (absQuadrantX + 1) << World.LOG2_CHUNK_QUADRANT_SIZE;
		int endY = (absQuadrantY + 1) << World.LOG2_CHUNK_QUADRANT_SIZE;

		Thread current = Thread.currentThread();
		Matrix3f mat = new Matrix3f();
		AutoBlockLayerInvalidation minInvalidation = AutoBlockLayerInvalidation.NONE;
		for(int x = startX; x < endX; x++) {
			for(int y = startY; y < endY; y++) {
				for(TileLayers layer : TileLayers.LAYERS) {
					if(current.isInterrupted()) {
						TileRenderingInternal.impl(source).close();
						return null;
					}
					TileLayer tileLayer = localWorld.layerFor(layer);
					TileVariant block = tileLayer.getBlock(x, y);
					TileRenderer renderer = block.getRenderer();
					renderer.renderTile(source,
						mat.identity().offset(x - startX, World.CHUNK_QUADRANT_SIZE - (y - startY)),
						// display y and real y are inverted
						localWorld,
						block,
						tileLayer.getBlockData(x, y),
						x,
						y
					);
					AutoBlockLayerInvalidation inv = renderer.whenInvalid();
					if(inv.ordinal() < minInvalidation.ordinal()) {
						minInvalidation = inv;
					}
				}
			}
		}

		RenderThread.queueRenderTask(() -> TileRenderingInternal.impl(source).bake());
		return source;
	}
}
