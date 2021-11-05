package net.devtech.jerraria.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.jerraria.world.internal.TickingWorld;

public class ChunkGroup {
	final TickingWorld world;
	public final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

	public ChunkGroup(TickingWorld world) {
		this.world = world;
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
			this.world.removeGroup(this);
		}
	}

	public void tick() {
		for(Chunk value : this.chunks.values()) {
			value.tick();
		}
	}
}
