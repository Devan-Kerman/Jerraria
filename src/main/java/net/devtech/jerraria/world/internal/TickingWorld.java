package net.devtech.jerraria.world.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.chunk.ChunkGroup;

public abstract class TickingWorld implements World {
	final Set<ChunkGroup> groups = new HashSet<>();
	final Set<Chunk> toRelink = new HashSet<>();
	final Executor executor;

	protected TickingWorld(Executor executor) {
		this.executor = executor;
	}

	public void requiresRelinking(Chunk chunk) {
		this.toRelink.add(chunk);
	}

	public void removeGroup(ChunkGroup group) {
		this.groups.remove(group);
	}

	public void createGroups() {
		while(!this.toRelink.isEmpty()) {
			ChunkGroup group = new ChunkGroup(this);
			Chunk chunk = this.toRelink.iterator().next();
			chunk.appendToGroup(group);
			for(Chunk value : group.chunks.values()) {
				this.toRelink.remove(value);
			}
		}
	}

	public void tick() {
		this.createGroups();
		List<CompletableFuture<Void>> ticks = new ArrayList<>();
		for(ChunkGroup group : this.groups) {
			ticks.add(CompletableFuture.runAsync(group::tick, this.executor));
		}
		ticks.forEach(CompletableFuture::join);
	}
}
