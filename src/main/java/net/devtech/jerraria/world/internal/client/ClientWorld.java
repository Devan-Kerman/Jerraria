package net.devtech.jerraria.world.internal.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.jerraria.world.WorldServer;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class ClientWorld extends AbstractClientWorld {
	public final Long2ObjectMap<ClientChunk> loadedChunkCache = new Long2ObjectOpenHashMap<>();

	public ClientWorld(WorldServer server, int id) {
		super(server, id);
	}

	@Override
	public boolean isChunkLoaded(int cx, int cy) {
		return this.loadedChunkCache.containsKey(Chunk.combineInts(cx, cy));
	}

	@Override
	public boolean isChunkAccessible(int cx, int cy) {
		// todo ask server
		// todo the best way to do this is to have the server send a list of all currently accessable chunks to the client
		// todo this way immersive portal stuff can still work, but we won't have to query the server every time we want to see if we can get a chunk
		return false;
	}

	@Override
	public Chunk getChunk(int x, int y) {
		return this.loadedChunkCache.get(Chunk.combineInts(x, y));
	}
}
