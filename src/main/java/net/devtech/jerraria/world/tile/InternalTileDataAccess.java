package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated internal
 */
@Deprecated
@ApiStatus.Internal
public class InternalTileDataAccess {
	public static TileLayers getLayer(TileData this_) {
		return this_.layer;
	}

	public static World getWorld(TileData this_) {
		return this_.world;
	}

	public static int getAbsX(TileData this_) {
		return this_.absX;
	}

	public static int getAbsY(TileData this_) {
		return this_.absY;
	}
}
