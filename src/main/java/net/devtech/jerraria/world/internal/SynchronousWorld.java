package net.devtech.jerraria.world.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.data.JCIO;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.world.WorldServer;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.ChunkGroup;

public class SynchronousWorld extends TickingWorld {
	public static final AtomicInteger SESSION_IDS = new AtomicInteger();
	final WorldServer server;
	final Path directory;
	final Long2ObjectMap<Chunk> loadedChunkCache = new Long2ObjectOpenHashMap<>();
	final ChunkReader reader;
	final int sessionId = SESSION_IDS.incrementAndGet();

	public SynchronousWorld(
		Path directory, Executor executor, boolean maintainOrder, WorldServer server) {
		super(executor, maintainOrder);
		this.directory = directory;
		this.server = server;
		this.reader = new ChunkReader();
	}

	@Override
	public void unloadGroup(ChunkGroup group) {
		for(var entry : group.chunks.long2ObjectEntrySet()) {
			long key = entry.getLongKey();
			this.unloadChunk(entry.getValue(), key);
		}
	}

	@Override
	public WorldServer getServer() {
		return this.server;
	}

	@Override
	public int sessionId() {
		return this.sessionId;
	}

	@Override
	public boolean isLoaded(int x, int y) {
		return this.loadedChunkCache.containsKey(Chunk.combineInts(x, y));
	}

	@Override
	public boolean canAccessImmediately(int x, int y) {
		return true;
	}

	private void unloadChunk(Chunk entry, long key) {
		this.loadedChunkCache.remove(key);
		Path chunkFile = SynchronousWorld.this.directory.resolve(Long.toHexString(key) + ".chunk");
		// output doesn't need buffering cus it's already buffered
		try(DataOutputStream stream = new DataOutputStream(Files.newOutputStream(chunkFile))) {
			JCIO.write(NativeJCType.TAG, entry.write(), stream);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	@Override
	public void unloadIndividualChunk(Chunk chunk) {
		this.unloadChunk(chunk, chunk.getId());
	}

	@Override
	public synchronized Chunk getChunk(int x, int y) {
		return this.loadedChunkCache.computeIfAbsent(Chunk.combineInts(x, y), this.reader);
	}

	class ChunkReader implements Long2ObjectFunction<Chunk> {
		Long2ObjectMap<Chunk> tempCache = new Long2ObjectOpenHashMap<>();
		ChunkGroup building;

		@Override
		public Chunk get(long key) {
			Chunk temp = this.tempCache.get(key);
			if(temp != null) {
				return temp;
			}

			Path chunkFile = SynchronousWorld.this.directory.resolve(Long.toHexString(key) + ".chunk");
			if(!Files.exists(chunkFile)) {
				return new Chunk(SynchronousWorld.this, Chunk.getA(key), Chunk.getB(key));
			}

			Chunk chunk = null;
			try(DataInputStream stream = new DataInputStream(new FastBufferedInputStream(Files.newInputStream(chunkFile)))) {
				chunk = new Chunk(
					SynchronousWorld.this,
					Chunk.getA(key),
					Chunk.getB(key),
					JCIO.read(NativeJCType.TAG, stream));
				this.tempCache.put(chunk.getId(), chunk);
				if(this.building == null) {
					this.building = new ChunkGroup(SynchronousWorld.this);
				}
				chunk.appendToGroup(this.building);
				chunk.resolve();
				return chunk;
			} catch(IOException e) {
				throw Validate.rethrow(e);
			} finally {
				groups.add(this.building);
				this.building = null;
				if(chunk != null) {
					this.tempCache.remove(chunk.getId());
				}
			}
		}
	}
}
