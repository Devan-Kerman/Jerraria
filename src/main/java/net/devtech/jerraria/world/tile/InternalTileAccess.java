package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated internal
 */
@Deprecated
@ApiStatus.Internal
public class InternalTileAccess {
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

	public static void init(TileData data, World world, int locationIndex, int chunkX, int chunkY) {
		data.world = world;
		final int layers = TileLayers.COUNT, size = World.CHUNK_SIZE;
		final int y = locationIndex / (layers * size);
		locationIndex -= (y * layers * size);
		final int x = locationIndex / layers;
		final int layer = locationIndex % layers;
		data.absX = chunkX * size + x;
		data.absY = chunkY * size + y;
		data.layer = TileLayers.LAYERS.get(layer);
	}

	public static JCElement<?> write(TileVariant variant, TileData data) {
		return variant.getOwner().write(data, variant);
	}

	public static TileData read(TileVariant variant, JCElement<?> view) {
		return variant.getOwner().read(variant, view);
	}
}
