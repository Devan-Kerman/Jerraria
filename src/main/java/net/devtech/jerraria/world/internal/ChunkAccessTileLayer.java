package net.devtech.jerraria.world.internal;

import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.ScheduledTick;
import net.devtech.jerraria.world.internal.chunk.TemporaryTileData;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.VariantConvertable;
import org.jetbrains.annotations.Nullable;

public class ChunkAccessTileLayer implements TileLayer {
	final TileLayers layers;
	final ChunkGetter getter;

	public ChunkAccessTileLayer(TileLayers layers, ChunkGetter getter) {
		this.layers = layers;
		this.getter = getter;
	}

	@Override
	public void scheduleTick(int x, int y, int delay) {
		this.addTemporaryTileData(ScheduledTick.TYPE, x, y, delay);
	}

	@Override
	public <T extends TemporaryTileData> T addTemporaryTileData(TemporaryTileData.Type<T> type, int x, int y, int delay) {
		return this.get((layer, chunk, localX, localY) -> {
			return chunk.schedule(type, layer, localX, localY, delay);
		}, x, y);
	}

	@Override
	public VariantConvertable getBlockAndData(int x, int y) {
		return this.get((layer, chunk, localX, localY) -> {
			TileData data = chunk.getData(layer, localX, localY);
			if(data != null) {
				return data;
			} else {
				return chunk.get(layer, localX, localY);
			}
		}, x, y);
	}

	@Override
	public TileVariant getBlock(int x, int y) {
		return this.get((layer, chunk, localX, localY) -> chunk.get(layer, localX, localY), x, y);
	}

	@Override
	public TileData getBlockData(int x, int y) {
		return this.get((layer, chunk, localX, localY) -> chunk.getData(layer, localX, localY), x, y);
	}

	@Override
	public @Nullable TileData putBlock(TileVariant variant, int x, int y, int flags) {
		Chunk chunk = this.getter.getChunk(x >> World.LOG2_CHUNK_SIZE, y >> World.LOG2_CHUNK_SIZE);
		int localX = x & World.CHUNK_MASK;
		int localY = y & World.CHUNK_MASK;
		TileData data = chunk.set(this.layers, localX, localY, variant);
		if((flags & SKIP_ON_PLACE) == 0) {
			//variant.onPlace(); (requires scheduling, pain) or does it? I don't think it does
		}
		return data;
	}

	<T> T get(BlockGetter<T> getter, int x, int y) {
		Chunk chunk = this.getter.getChunk(x >> World.LOG2_CHUNK_SIZE, y >> World.LOG2_CHUNK_SIZE);
		int localX = x & World.CHUNK_MASK;
		int localY = y & World.CHUNK_MASK;
		return getter.getValue(this.layers, chunk, localX, localY);
	}

	public interface BlockGetter<T> {
		T getValue(TileLayers layers, Chunk chunk, int localX, int localY);
	}
	public interface ChunkGetter {
		Chunk getChunk(int chunkX, int chunkY);
	}
}
