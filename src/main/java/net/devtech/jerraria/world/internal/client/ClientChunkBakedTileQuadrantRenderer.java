package net.devtech.jerraria.world.internal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.ShaderSource;
import net.devtech.jerraria.world.tile.render.TileRenderer;

public class ClientChunkBakedTileQuadrantRenderer {
	public static ClientChunk.BakedClientChunkQuadrant bake(World localWorld, int absQuadrantX, int absQuadrantY) {
		ShaderSource source = new ShaderSource();
		int startX = absQuadrantX << World.LOG2_CHUNK_QUADRANT_SIZE;
		int startY = absQuadrantY << World.LOG2_CHUNK_QUADRANT_SIZE;
		int endX = (absQuadrantX + 1) << World.LOG2_CHUNK_QUADRANT_SIZE;
		int endY = (absQuadrantY + 1) << World.LOG2_CHUNK_QUADRANT_SIZE;

		Thread current = Thread.currentThread();
		Matrix3f mat = new Matrix3f();
		for(int x = startX; x < endX; x++) {
			for(int y = startY; y < endY; y++) {
				for(TileLayers layer : TileLayers.LAYERS) {
					if(current.isInterrupted()) {
						source.close();
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
				}
			}
		}


		// group identical BuiltGlStates together to avoid context switching
		// maybe sorting to minimize changes at some point?
		Map<BuiltGlState, List<ClientChunk.BakedClientChunkQuadrantData>> data = new HashMap<>();
		for(var entry : source.entries()) {
			if(current.isInterrupted()) {
				source.close();
				return null;
			}
			var value = entry.getKey();
			data
				.computeIfAbsent(value.state(), s -> new ArrayList<>())
				.add(new ClientChunk.BakedClientChunkQuadrantData(value.invalidation(),
					entry.getValue(),
					value.config(),
					value.primitive(),
					value.state()
				));
		}

		// todo order independent translucency

		return new ClientChunk.BakedClientChunkQuadrant(data.values().stream().flatMap(List::stream).toList(),
			List.of()
		);
	}
}
