package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.Nullable;

// todo make TileData extend this to reduce redundant objects
public abstract class TemporaryTileData extends UnpositionedTileData {
	public final Type<?> type;
	final TileLayers layer;
	final int localX, localY;
	int counter;

	protected TemporaryTileData(Type<?> type, TileLayers layer, int localX, int localY, int time) {
		this.type = type;
		this.layer = layer;
		this.localX = localX;
		this.localY = localY;
		this.counter = time;
	}

	protected TemporaryTileData(Type<?> type, long packedData) {
		this.type = type;
		this.counter = (int) packedData;
		packedData >>= 32;
		this.localY = (int) (packedData & World.CHUNK_MASK);
		packedData >>= World.LOG2_CHUNK_SIZE;
		this.localX = (int) (packedData & World.CHUNK_MASK);
		packedData >>= World.LOG2_CHUNK_SIZE;
		this.layer = TileLayers.LAYERS.get((int) (packedData & TileLayers.COUNT_MASK));
	}

	/**
	 * Set the number of ticks until the task must be executed
	 */
	public void setTime(int counter) {
		this.counter = counter;
	}

	protected abstract void onInvalidated(Chunk chunk, World world, TileVariant variant, @Nullable TileData data, TileLayers layers, int x, int y);


	@Override
	TileLayers getLayer() {
		return this.layer;
	}

	@Override
	int getLocalX() {
		return this.localX;
	}

	@Override
	int getLocalY() {
		return this.localY;
	}

	@Override
	int getCounter() {
		return this.counter;
	}

	@Override
	void setCounter(int counter) {
		this.counter = counter;
	}

	@Override
	public Type<?> getType() {
		return this.type;
	}
}
