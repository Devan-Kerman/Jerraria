package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.Internal
public class InternalTileDataAccess {

	public static int getAbsX(InternalTileData this_) {
		return this_.absX;
	}

	public static int getAbsY(InternalTileData this_) {
		return this_.absY;
	}

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
