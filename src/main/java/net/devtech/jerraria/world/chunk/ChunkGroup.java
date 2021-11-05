package net.devtech.jerraria.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.TickingWorld;

public class ChunkGroup {
	final TickingWorld backing;
	final World local;
	public final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

	public ChunkGroup(TickingWorld world) {
		this.backing = world;
		this.local = new World();
	}

	public boolean contains(Chunk chunk) {
		return this.chunks.containsKey(chunk.getId());
	}

	public void add(Chunk chunk) {
		this.chunks.put(chunk.getId(), chunk);
	}

	public void remove(Chunk chunk) {
		this.chunks.remove(chunk.getId());
		if(this.chunks.isEmpty()) {
			this.backing.removeGroup(this);
		}
	}

	public void tick() {
		for(Chunk value : this.chunks.values()) {
			value.tick();
		}
	}

	class World extends AbstractWorld {
		@Override
		protected Chunk getChunk(int x, int y) {
			Chunk chunk = ChunkGroup.this.chunks.get(Chunk.combineInts(x, y));
			if(chunk == null) {
				throw new IllegalStateException("chunk does not belong to group, you must link to a chunk before using it!");
			}
			return chunk;
		}
	}
}
