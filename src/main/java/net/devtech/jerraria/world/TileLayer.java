package net.devtech.jerraria.world;

import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.world.internal.chunk.TemporaryTileData;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.VariantConvertable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TileLayer {
	int SKIP_ON_PLACE = 1;

	/**
	 * Calls the Tile at the given position's {@link Tile#onScheduledTick(World, TileVariant, TileData, TileLayers, int, int)} method after the given delay
	 */
	void scheduleTick(int x, int y, int delay);

	<T extends TemporaryTileData> T addTemporaryTileData(TemporaryTileData.Type<T> type, int x, int y, int delay);

	VariantConvertable getBlockAndData(int x, int y);

	TileVariant getBlock(int x, int y);

	@Nullable
	TileData getBlockData(int x, int y);

	/**
	 * puts the block at the given location, creates stack tile data for that location if the TileVariant has data
	 *
	 * @param flags todo document
	 * @return the newly created TileData (or if the TileVariant does not have a tile data, it returns the set tile variant)
	 * 	for the given variant if the block was successfully set, otherwise it will return the block already at the location.
	 * 	If {@link VariantConvertable#getVariant()} != {@param variant} then the block, for whatever reason, could not be placed.
	 */
	@NotNull
	VariantConvertable setBlock(TileVariant variant, int x, int y, int flags);

	/**
	 * puts the block at the given location, creates stack tile data for that location if the TileVariant has data
	 *
	 * @param flags todo document
	 */
	VariantConvertable putBlock(TileVariant variant, int x, int y, int flags);

	/**
	 * @see #setBlock(TileVariant, int, int, int)
	 */
	@NotNull
	default VariantConvertable setBlock(TileVariant variant, int x, int y) {
		return this.setBlock(variant, x, y, 0);
	}

	default VariantConvertable forcePutBlock(TileVariant block, int x, int y) {
		return this.forcePutBlock(block, 0, 0, 0, 0);
	}

	default VariantConvertable forcePutBlock(TileVariant block, int x, int y, int removeFlags, int setFlags) {
		VariantConvertable variant = this.setBlock(Tiles.AIR.getDefaultVariant(), x, y, removeFlags);
		if(variant.getVariant() != Tiles.AIR.getDefaultVariant()) {
			return this.setBlock(block, x, y, setFlags);
		} else {
			return variant;
		}
	}
}
