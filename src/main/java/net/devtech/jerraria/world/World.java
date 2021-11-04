package net.devtech.jerraria.world;

import net.devtech.jerraria.tile.TileData;
import net.devtech.jerraria.tile.TileVariant;

public interface World {
	TileVariant getBlock(int x, int y, int z);

	TileData getBlockData(int x, int y, int z);
}
