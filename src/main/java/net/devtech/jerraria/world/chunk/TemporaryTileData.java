package net.devtech.jerraria.world.chunk;

import java.util.List;

import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class TemporaryTileData {
	final TileLayers layer;
	final int localX, localY;
	int counter;
	public final int delay;

	TemporaryTileData(TileLayers layer, int localX, int localY, int delay) {
		this.layer = layer;
		this.localX = localX;
		this.localY = localY;
		this.delay = delay;
	}

	public abstract void onInvalidated(TileVariant variant, @Nullable TileData data, World world, int x, int y);

	/**
	 * When a block is replaced in a chunk, this method is called to determine whether the scheduled action should still be run at the alloted time
	 */
	public abstract boolean isIncompatible(TileVariant old, TileVariant new_);

	// todo serialize n stuff, pain pain pain

	public static class TempLink extends TemporaryTileData {
		final Chunk chunk;
		final List<Chunk> links;

		TempLink(TileLayers layer, int localX, int localY, int delay, Chunk chunk, List<Chunk> links) {
			super(layer, localX, localY, delay);
			this.chunk = chunk;
			this.links = links;
		}

		@Override
		public void onInvalidated(TileVariant variant, @Nullable TileData data, World world, int x, int y) {
			for(Chunk link : this.links) {
				this.chunk.removeLink(link);
			}
		}

		@Override
		public boolean isIncompatible(TileVariant old, TileVariant new_) {
			return false;
		}
	}

}
