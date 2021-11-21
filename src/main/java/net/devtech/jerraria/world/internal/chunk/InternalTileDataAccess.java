package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;

public class InternalTileDataAccess {
	public static void init(InternalTileData data, int locationIndex, int chunkX, int chunkY) {
		final int layers = TileLayers.COUNT, size = World.CHUNK_SIZE;
		final int y = locationIndex / (layers * size);
		locationIndex -= (y * layers * size);
		final int x = locationIndex / layers;
		final int layer = locationIndex % layers;
		data.absX = chunkX * size + x;
		data.absY = chunkY * size + y;
		data.layer = TileLayers.LAYERS.get(layer);
	}
}
