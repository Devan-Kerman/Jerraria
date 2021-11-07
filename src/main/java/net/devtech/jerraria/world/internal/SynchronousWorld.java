package net.devtech.jerraria.world.internal;

import java.nio.file.Path;
import java.util.concurrent.Executor;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.chunk.ChunkGroup;

public class SynchronousWorld extends TickingWorld {
	final Path directory;
	final Long2ObjectMap<Chunk> loadedChunkCache;
	final ChunkReader reader;

	public SynchronousWorld(Path directory, Executor executor, Long2ObjectMap<Chunk> cache) {
		super(executor);
		this.directory = directory;
		this.loadedChunkCache = cache;
		this.reader = new ChunkReader();
	}

	@Override
	public void unloadGroup(ChunkGroup group) {
	}

	@Override
	protected synchronized Chunk getChunk(int x, int y) {
		return this.loadedChunkCache.computeIfAbsent(Chunk.combineInts(x, y), this.reader);
	}

	class ChunkReader implements Long2ObjectFunction<Chunk> {
		@Override
		public Chunk get(long key) {
			Path chunkFile = SynchronousWorld.this.directory.resolve(Long.toHexString(key) + ".chunk");
			return null;
		}
	}
}
