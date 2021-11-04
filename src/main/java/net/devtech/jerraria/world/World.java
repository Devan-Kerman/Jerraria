package net.devtech.jerraria.world;

import net.devtech.jerraria.tile.TileData;
import net.devtech.jerraria.tile.TileVariant;

public interface World {
	int LOG2_CHUNK_SIZE = 8;
	int CHUNK_SIZE = 1 << 8;
	int CHUNK_MASK = CHUNK_SIZE - 1;

	TileVariant getBlock(int x, int y, int z);

	TileData getBlockData(int x, int y, int z);
}
