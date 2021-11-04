package net.devtech.jerraria.world.chunk;

import net.devtech.jerraria.tile.TileData;
import net.devtech.jerraria.tile.TileVariant;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class ScheduledTileAction {
	final TileLayers layer;
	final int localX, localY;
	int counter;
	public final int delay;

	ScheduledTileAction(TileLayers layer, int localX, int localY, int delay) {
		this.layer = layer;
		this.localX = localX;
		this.localY = localY;
		this.delay = delay;
	}

	public abstract void run(TileVariant variant, @Nullable TileData data, World world, int x, int y);

	/**
	 * When a block is replaced in a chunk, this method is called to determine whether the scheduled action should still be run at the alloted time
	 */
	public abstract boolean isIncompatible(TileVariant old, TileVariant new_);

}
