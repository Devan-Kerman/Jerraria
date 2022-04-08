package net.devtech.jerraria.world.internal.client;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.ShaderSource;
import net.devtech.jerraria.world.tile.render.TileRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClientChunkBakedTileQuadrantRenderer {
	public static ClientChunk.BakedClientChunkQuadrant bake(World localWorld, int absQuadrantX, int absQuadrantY) {
		ShaderSource source = new ShaderSource();
		int startX = absQuadrantX << World.LOG2_CHUNK_QUADRANT_SIZE,
			startY = absQuadrantY << World.LOG2_CHUNK_QUADRANT_SIZE,
			endX = (absQuadrantX + 1) << World.LOG2_CHUNK_QUADRANT_SIZE,
			endY = (absQuadrantY + 1) << World.LOG2_CHUNK_QUADRANT_SIZE;

		Thread current = Thread.currentThread();
		Matrix3f mat = new Matrix3f();
		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				for (TileLayers layer : TileLayers.LAYERS) {
					if(current.isInterrupted()) {
						return null;
					}
					TileLayer tileLayer = localWorld.layerFor(layer);
					TileVariant block = tileLayer.getBlock(x, y);
					TileRenderer renderer = block.getRenderer();
					renderer.renderTile(
						source,
						mat.identity().offset(x, y),
						localWorld,
						block,
						tileLayer.getBlockData(x, y),
						x,
						y
					);
				}
			}
		}

		List<ClientChunk.BakedClientChunkQuadrantData> data = new ArrayList<>();

		var sorted = new ArrayList<>(source.keySet());
		sorted.sort(Comparator.comparing(t -> t.getKey().value()));
		for (var entry : sorted) {
			if(current.isInterrupted()) {
				return null;
			}
			var value = entry.getValue();
			data.add(new ClientChunk.BakedClientChunkQuadrantData(value.invalidation(), value.copied(), value.configurator(), value.primitive()));
		}

		// todo translucency sorting, pain
		// translucency sorting might need to be global, which will be literal hell

		return new ClientChunk.BakedClientChunkQuadrant(data, List.of());
	}
}
