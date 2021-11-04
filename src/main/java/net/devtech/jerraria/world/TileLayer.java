package net.devtech.jerraria.world;

import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.VariantConvertable;

public interface TileLayer {

	VariantConvertable getBlockAndData(int x, int y, int z);

	TileVariant getBlock(int x, int y, int z);

	TileData getBlockData(int x, int y, int z);
}
