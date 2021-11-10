package net.devtech.jerraria.world;

import net.devtech.jerraria.world.internal.chunk.TemporaryTileData;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.VariantConvertable;
import org.jetbrains.annotations.Nullable;

public interface TileLayer {
	int SKIP_ON_PLACE = 1;

	void scheduleTick(int x, int y, int delay);

	<T extends TemporaryTileData> T addTemporaryTileData(TemporaryTileData.Type<T> type, int x, int y, int delay);

	VariantConvertable getBlockAndData(int x, int y);

	TileVariant getBlock(int x, int y);

	@Nullable
	TileData getBlockData(int x, int y);

	@Nullable
	default TileData putBlock(TileVariant variant, int x, int y) {
		return this.putBlock(variant, x, y, 0);
	}

	/**
	 * puts the block at the given location, creates a tile data for that location if the TileVariant has data
	 */
	@Nullable
	TileData putBlock(TileVariant variant, int x, int y, int flags);
}
